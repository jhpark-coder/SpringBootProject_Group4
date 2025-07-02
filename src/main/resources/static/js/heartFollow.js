// 구독 기능 JavaScript

// 구독 토글 함수 (사용자 ID 기반)
function toggleFollow(userId) {
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

            // 구독 버튼 상태 업데이트 (기존 방식)
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

            // productDetail 페이지의 Follow 버튼 상태 업데이트 (새로운 방식)
            const followButtons = document.querySelectorAll('.follow-btn');
            followButtons.forEach(button => {
                const onclickAttr = button.getAttribute('onclick');
                if (onclickAttr && onclickAttr.includes(`toggleFollow(${userId})`)) {
                    if (data.isFollowing) {
                        button.textContent = 'Following'; // 실제 텍스트 유지 (CSS에서 투명처리)
                        button.classList.add('following');
                        console.log('Following 클래스 추가됨:', button.classList);
                    } else {
                        button.textContent = 'Follow';
                        button.classList.remove('following');
                        console.log('Following 클래스 제거됨:', button.classList);
                    }
                }
            });

            // 마이페이지의 Subscribe 버튼들 상태 업데이트 (onclick 속성으로 구분)
            const myPageFollowButtons = document.querySelectorAll('.product-card .follow-btn');
            myPageFollowButtons.forEach(button => {
                const onclickAttr = button.getAttribute('onclick');
                if (onclickAttr && onclickAttr.includes(`toggleFollow(${userId})`)) {
                    if (data.isFollowing) {
                        button.textContent = 'Following'; // 실제 텍스트 유지 (CSS에서 투명처리)
                        button.classList.add('following');
                    } else {
                        button.textContent = 'Follow';
                        button.classList.remove('following');
                    }
                }
            });

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

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    // 구독 통계 로드 (사용자 프로필 페이지 등에서 사용)
    const userIdElements = document.querySelectorAll('[data-user-id]');
    userIdElements.forEach(element => {
        const userId = element.getAttribute('data-user-id');
        if (userId) {
            loadFollowStats(userId);
        }
    });
});

// 전역 함수로 등록
window.toggleFollow = toggleFollow;
window.toggleLike = toggleLike;
window.loadFollowStats = loadFollowStats; 