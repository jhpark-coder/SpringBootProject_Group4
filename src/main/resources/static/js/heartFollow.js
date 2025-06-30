// 구독 기능 JavaScript

// 현재 로그인한 사용자 ID를 가져오는 함수
function getCurrentUserId() {
    // 세션에서 로그인 정보 확인
    return fetch('/api/test/login-status')
        .then(response => response.json())
        .then(data => {
            if (data.loggedIn) {
                return data.userId;
            } else {
                // 로그인하지 않은 경우 테스트용 ID 반환
                return 1;
            }
        })
        .catch(error => {
            console.error('로그인 상태 확인 오류:', error);
            // 오류 시 테스트용 ID 반환
            return 1;
        });
}

// 구독 토글 함수 (사용자 ID 기반)
async function toggleFollow(userId) {
    // 현재 로그인한 사용자 ID 가져오기
    const currentUserId = await getCurrentUserId();
    
    if (currentUserId === userId) {
        alert('자기 자신을 구독할 수 없습니다.');
        return;
    }
    
    fetch(`/api/follow/${userId}`, {
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
        const followButton = document.querySelector(`[data-follow-user-id="${userId}"]`);
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
        const followerCountElement = document.querySelector(`[data-follower-count="${userId}"]`);
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

// 좋아요 토글 함수 (상품 ID 기반)
function toggleLike(productId) {
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
        
        // 좋아요 버튼 상태 업데이트
        const likeButton = document.querySelector(`[data-product-id="${productId}"]`);
        if (likeButton) {
            if (data.isLiked) {
                likeButton.classList.add('liked');
            } else {
                likeButton.classList.remove('liked');
            }
            
            // 좋아요 수 업데이트
            const likeCountElement = likeButton.querySelector('.like-count');
            if (likeCountElement) {
                likeCountElement.textContent = data.heartCount;
            }
        }
        
        console.log('좋아요 토글 결과:', data);
    })
    .catch(error => {
        console.error('좋아요 토글 오류:', error);
        alert('좋아요 기능을 사용할 수 없습니다.');
    });
}

// 구독 통계 로드 함수 (사용자 ID 기반)
function loadFollowStats(userId) {
    fetch(`/api/follow/${userId}/stats`)
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            console.error('구독 통계 로드 오류:', data.message);
            return;
        }
        
        const followerCountElement = document.querySelector(`[data-follower-count="${userId}"]`);
        const followingCountElement = document.querySelector(`[data-following-count="${userId}"]`);
        
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

// 구독 상태 확인 함수 (사용자 ID 기반)
async function checkFollowStatus(userId) {
    // 현재 로그인한 사용자 ID 가져오기
    const currentUserId = await getCurrentUserId();
    
    // 본인인 경우 버튼 비활성화
    const followButton = document.querySelector(`[data-follow-user-id="${userId}"]`);
    if (followButton && currentUserId === userId) {
        followButton.disabled = true;
        followButton.textContent = '본인';
        followButton.classList.add('disabled');
        followButton.style.opacity = '0.5';
        followButton.style.cursor = 'not-allowed';
        console.log(`사용자 ${userId}는 본인이므로 구독 버튼 비활성화`);
        return;
    }
    
    fetch(`/api/follow/${userId}/status`)
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            console.error('구독 상태 확인 오류:', data.message);
            return;
        }
        
        // 구독 버튼 상태 업데이트
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
        const followerCountElement = document.querySelector(`[data-follower-count="${userId}"]`);
        if (followerCountElement) {
            followerCountElement.textContent = data.followerCount;
        }
        
        console.log(`사용자 ${userId} 구독 상태 확인 완료:`, data);
    })
    .catch(error => {
        console.error(`사용자 ${userId} 구독 상태 확인 오류:`, error);
    });
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function() {
    // 구독 통계 로드 (사용자 프로필 페이지 등에서 사용)
    const userIdElements = document.querySelectorAll('[data-user-id]');
    userIdElements.forEach(element => {
        const userId = element.getAttribute('data-user-id');
        if (userId) {
            loadFollowStats(userId);
        }
    });
    
    // 구독 상태 확인 및 버튼 동기화
    const followButtons = document.querySelectorAll('[data-follow-user-id]');
    for (const button of followButtons) {
        const userId = button.getAttribute('data-follow-user-id');
        if (userId) {
            await checkFollowStatus(userId);
        }
    }
    
    // data-user-id 속성이 있는 요소들도 확인
    const userElements = document.querySelectorAll('[data-user-id]');
    for (const element of userElements) {
        const userId = element.getAttribute('data-user-id');
        if (userId) {
            await checkFollowStatus(userId);
        }
    }
});

// 전역 함수로 등록
window.toggleFollow = toggleFollow;
window.toggleLike = toggleLike;
window.loadFollowStats = loadFollowStats;
window.checkFollowStatus = checkFollowStatus; 