// 찜하기 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // 알림 메시지 표시 함수
    function showNotification(message, type = 'success') {
        const notification = document.getElementById('notification');
        const messageElement = document.getElementById('notification-message');
        
        messageElement.textContent = message;
        notification.className = `notification ${type}`;
        notification.classList.remove('hidden');
        
        // 3초 후 자동으로 숨기기
        setTimeout(() => {
            notification.classList.add('hidden');
        }, 3000);
    }
    
    // 알림 메시지 닫기
    document.querySelector('.notification-close').addEventListener('click', function() {
        document.getElementById('notification').classList.add('hidden');
    });
    
    // 개별 상품 찜하기 제거
    function removeFromWishlist(productId) {
        fetch(`/wishlist/remove/${productId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification(data.message, 'success');
                // 해당 상품 카드 제거
                const productCard = document.querySelector(`[data-product-id="${productId}"]`).closest('.wishlist-item');
                productCard.style.animation = 'fadeOut 0.3s ease';
                setTimeout(() => {
                    productCard.remove();
                    updateWishlistCount();
                    checkEmptyWishlist();
                }, 300);
            } else {
                showNotification(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showNotification('찜하기 제거 중 오류가 발생했습니다.', 'error');
        });
    }
    
    // 찜하기 개수 업데이트
    function updateWishlistCount() {
        const wishlistItems = document.querySelectorAll('.wishlist-item');
        const countElement = document.querySelector('.wishlist-count span');
        if (countElement) {
            countElement.textContent = wishlistItems.length;
        }
    }
    
    // 빈 찜하기 목록 확인
    function checkEmptyWishlist() {
        const wishlistGrid = document.querySelector('.wishlist-grid');
        const emptyWishlist = document.querySelector('.empty-wishlist');
        const clearAllButton = document.getElementById('clearAllWishlist');
        
        if (wishlistGrid && wishlistGrid.children.length === 0) {
            // 찜한 상품이 없으면 빈 상태 표시
            if (emptyWishlist) {
                emptyWishlist.style.display = 'block';
            }
            if (clearAllButton) {
                clearAllButton.style.display = 'none';
            }
        }
    }
    
    // 전체 찜하기 삭제
    function clearAllWishlist() {
        if (confirm('모든 찜한 상품을 삭제하시겠습니까?')) {
            const wishlistItems = document.querySelectorAll('.wishlist-item');
            const productIds = Array.from(wishlistItems).map(item => 
                item.querySelector('[data-product-id]').getAttribute('data-product-id')
            );
            
            // 모든 상품을 순차적으로 삭제
            let deletedCount = 0;
            productIds.forEach((productId, index) => {
                setTimeout(() => {
                    removeFromWishlist(productId);
                    deletedCount++;
                    
                    if (deletedCount === productIds.length) {
                        showNotification('모든 찜한 상품이 삭제되었습니다.', 'success');
                    }
                }, index * 100); // 100ms 간격으로 삭제
            });
        }
    }
    
    // 이벤트 리스너 등록
    
    // 개별 삭제 버튼 클릭
    document.addEventListener('click', function(e) {
        if (e.target.closest('.btn-remove')) {
            const productId = e.target.closest('.btn-remove').getAttribute('data-product-id');
            removeFromWishlist(productId);
        }
        
        if (e.target.closest('.remove-wishlist')) {
            const productId = e.target.closest('.remove-wishlist').getAttribute('data-product-id');
            removeFromWishlist(productId);
        }
    });
    
    // 전체 삭제 버튼 클릭
    const clearAllButton = document.getElementById('clearAllWishlist');
    if (clearAllButton) {
        clearAllButton.addEventListener('click', clearAllWishlist);
    }
    
    // 상품 카드 호버 효과
    document.addEventListener('mouseenter', function(e) {
        if (e.target.closest('.wishlist-item')) {
            const item = e.target.closest('.wishlist-item');
            item.style.transform = 'translateY(-5px)';
        }
    }, true);
    
    document.addEventListener('mouseleave', function(e) {
        if (e.target.closest('.wishlist-item')) {
            const item = e.target.closest('.wishlist-item');
            item.style.transform = 'translateY(0)';
        }
    }, true);
    
    // 이미지 로드 실패 시 기본 이미지로 대체
    document.addEventListener('error', function(e) {
        if (e.target.tagName === 'IMG') {
            e.target.src = '/images/default-product.jpg';
        }
    }, true);
    
    // 페이지 로드 시 초기화
    updateWishlistCount();
    checkEmptyWishlist();
    
    // CSS 애니메이션 추가
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeOut {
            from {
                opacity: 1;
                transform: scale(1);
            }
            to {
                opacity: 0;
                transform: scale(0.8);
            }
        }
        
        .wishlist-item {
            transition: all 0.3s ease;
        }
        
        .wishlist-item:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.15);
        }
    `;
    document.head.appendChild(style);
    
    // 키보드 접근성 개선
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' || e.key === ' ') {
            const focusedElement = document.activeElement;
            if (focusedElement.classList.contains('btn-remove') || 
                focusedElement.classList.contains('remove-wishlist')) {
                e.preventDefault();
                const productId = focusedElement.getAttribute('data-product-id');
                removeFromWishlist(productId);
            }
        }
    });
    
    // 터치 디바이스 지원
    let touchStartY = 0;
    let touchEndY = 0;
    
    document.addEventListener('touchstart', function(e) {
        touchStartY = e.changedTouches[0].screenY;
    });
    
    document.addEventListener('touchend', function(e) {
        touchEndY = e.changedTouches[0].screenY;
        handleSwipe();
    });
    
    function handleSwipe() {
        const swipeThreshold = 50;
        const diff = touchStartY - touchEndY;
        
        if (Math.abs(diff) > swipeThreshold) {
            // 스와이프 제스처 처리 (필요시 구현)
        }
    }
    
    // 성능 최적화: 디바운싱
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
    
    // 윈도우 리사이즈 시 그리드 재조정
    const debouncedResize = debounce(function() {
        const wishlistGrid = document.querySelector('.wishlist-grid');
        if (wishlistGrid) {
            // 그리드 레이아웃 재계산
            wishlistGrid.style.display = 'none';
            setTimeout(() => {
                wishlistGrid.style.display = 'grid';
            }, 10);
        }
    }, 250);
    
    window.addEventListener('resize', debouncedResize);
    
    // 로딩 상태 표시
    function showLoading() {
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'loading';
        loadingDiv.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 처리 중...';
        loadingDiv.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: rgba(0,0,0,0.8);
            color: white;
            padding: 20px;
            border-radius: 10px;
            z-index: 9999;
        `;
        document.body.appendChild(loadingDiv);
    }
    
    function hideLoading() {
        const loadingDiv = document.querySelector('.loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }
    }
    
    // 글로벌 함수로 등록 (다른 스크립트에서 사용 가능)
    window.wishlistUtils = {
        showNotification,
        removeFromWishlist,
        clearAllWishlist,
        showLoading,
        hideLoading
    };
}); 