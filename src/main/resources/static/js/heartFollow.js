// 찜하기 및 구독 기능 JavaScript

// 찜하기 토글 함수
function toggleHeart(productId) {
    fetch(`/api/products/${productId}/heart`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert(data.message);
            return;
        }
        
        // 하트 버튼 상태 업데이트
        const heartButton = document.querySelector(`[data-product-id="${productId}"]`);
        if (heartButton) {
            if (data.isLiked) {
                heartButton.classList.add('liked');
                heartButton.innerHTML = '<i class="fas fa-heart"></i>';
            } else {
                heartButton.classList.remove('liked');
                heartButton.innerHTML = '<i class="far fa-heart"></i>';
            }
        }
        
        // 좋아요 수 업데이트
        const heartCountElement = document.querySelector(`[data-heart-count="${productId}"]`);
        if (heartCountElement) {
            heartCountElement.textContent = data.heartCount;
        }
        
        console.log('찜하기 토글 결과:', data);
    })
    .catch(error => {
        console.error('찜하기 토글 오류:', error);
        alert('찜하기 기능을 사용할 수 없습니다.');
    });
}

// 구독 토글 함수
function toggleFollow(username) {
    fetch(`/api/follow/${username}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert(data.message);
            return;
        }
        
        // 구독 버튼 상태 업데이트
        const followButton = document.querySelector(`[data-follow-username="${username}"]`);
        if (followButton) {
            if (data.isFollowing) {
                followButton.classList.add('following');
                followButton.textContent = '구독 중';
            } else {
                followButton.classList.remove('following');
                followButton.textContent = '구독하기';
            }
        }
        
        // 팔로워 수 업데이트
        const followerCountElement = document.querySelector(`[data-follower-count="${username}"]`);
        if (followerCountElement) {
            followerCountElement.textContent = data.followerCount;
        }
        
        console.log('구독 토글 결과:', data);
    })
    .catch(error => {
        console.error('구독 토글 오류:', error);
        alert('구독 기능을 사용할 수 없습니다.');
    });
}

// 찜한 상품 목록 로드 함수
function loadLikedProducts(page = 0) {
    fetch(`/api/liked-products?page=${page}&size=12`)
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            console.error('찜한 상품 목록 로드 오류:', data.message);
            return;
        }
        
        const container = document.getElementById('liked-products-container');
        if (!container) return;
        
        if (data.products.length === 0) {
            container.innerHTML = '<p class="no-products">찜한 상품이 없습니다.</p>';
            return;
        }
        
        let html = '';
        data.products.forEach(product => {
            html += `
                <div class="product-card">
                    <img src="${product.imageUrl}" alt="${product.name}" class="product-image">
                    <div class="product-info">
                        <h3>${product.name}</h3>
                        <p class="price">₩${product.price.toLocaleString()}</p>
                        <p class="category">${product.primaryCategory} > ${product.secondaryCategory}</p>
                        <div class="product-actions">
                            <button onclick="toggleHeart(${product.id})" class="heart-btn liked" data-product-id="${product.id}">
                                <i class="fas fa-heart"></i>
                            </button>
                            <a href="/products/${product.id}" class="view-btn">상세보기</a>
                        </div>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
        
        // 페이징 정보 업데이트
        updatePagination(data.currentPage, data.totalPages);
        
        console.log('찜한 상품 목록 로드 완료:', data);
    })
    .catch(error => {
        console.error('찜한 상품 목록 로드 오류:', error);
    });
}

// 구독 통계 로드 함수
function loadFollowStats(username) {
    fetch(`/api/follow/${username}/stats`)
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            console.error('구독 통계 로드 오류:', data.message);
            return;
        }
        
        const followerCountElement = document.querySelector(`[data-follower-count="${username}"]`);
        const followingCountElement = document.querySelector(`[data-following-count="${username}"]`);
        
        if (followerCountElement) {
            followerCountElement.textContent = data.followerCount;
        }
        if (followingCountElement) {
            followingCountElement.textContent = data.followingCount;
        }
        
        console.log('구독 통계 로드 완료:', data);
    })
    .catch(error => {
        console.error('구독 통계 로드 오류:', error);
    });
}

// 찜한 상품 통계 로드 함수
function loadLikedProductsStats() {
    fetch('/api/liked-products/stats')
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            console.error('찜한 상품 통계 로드 오류:', data.message);
            return;
        }
        
        const likedCountElement = document.getElementById('liked-products-count');
        if (likedCountElement) {
            likedCountElement.textContent = data.likedProductCount;
        }
        
        console.log('찜한 상품 통계 로드 완료:', data);
    })
    .catch(error => {
        console.error('찜한 상품 통계 로드 오류:', error);
    });
}

// 페이징 업데이트 함수
function updatePagination(currentPage, totalPages) {
    const paginationContainer = document.getElementById('pagination');
    if (!paginationContainer) return;
    
    let html = '';
    
    // 이전 페이지 버튼
    if (currentPage > 0) {
        html += `<button onclick="loadLikedProducts(${currentPage - 1})" class="page-btn">이전</button>`;
    }
    
    // 페이지 번호들
    for (let i = 0; i < totalPages; i++) {
        if (i === currentPage) {
            html += `<span class="page-btn current">${i + 1}</span>`;
        } else {
            html += `<button onclick="loadLikedProducts(${i})" class="page-btn">${i + 1}</button>`;
        }
    }
    
    // 다음 페이지 버튼
    if (currentPage < totalPages - 1) {
        html += `<button onclick="loadLikedProducts(${currentPage + 1})" class="page-btn">다음</button>`;
    }
    
    paginationContainer.innerHTML = html;
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 찜한 상품 페이지인 경우
    if (window.location.pathname === '/liked-products') {
        loadLikedProducts();
        loadLikedProductsStats();
    }
    
    // 구독 통계 로드 (사용자 프로필 페이지 등에서 사용)
    const usernameElements = document.querySelectorAll('[data-username]');
    usernameElements.forEach(element => {
        const username = element.getAttribute('data-username');
        if (username) {
            loadFollowStats(username);
        }
    });
});

// 전역 함수로 등록
window.toggleHeart = toggleHeart;
window.toggleFollow = toggleFollow;
window.loadLikedProducts = loadLikedProducts;
window.loadFollowStats = loadFollowStats;
window.loadLikedProductsStats = loadLikedProductsStats; 