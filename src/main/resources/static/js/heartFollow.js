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

//function requestPay() {
//    IMP.init("imp20067661"); // 실제 가맹점 코드로 바꿔주세요
//
//    IMP.request_pay({
//        pg: "html5_inicis",         // 결제 대행사
//        pay_method: "card",         // 결제 수단
//        merchantuid: "order" + new Date().getTime(), // 주문번호 (고유해야 함)
//        name: "상품명",
//        amount: 1000                // 결제 금액 (원 단위)
//    }, function (rsp) {
//        if (rsp.success) {
//            alert("결제 성공!");
//            console.log(rsp);
//        } else {
//            alert("결제 실패: " + rsp.error_msg);
//        }
//    });
//}
function requestPay() {
    const productName = $("#productName").val();
    const productAmount = parseInt($("#productAmount").val(), 10);

    IMP.init("imp20067661");

    IMP.request_pay({
        pg: "html5_inicis",
        pay_method: "card",
        merchantuid: "order" + new Date().getTime(),
        name: productName,
        amount: 100
    }, function (rsp) {
        if (rsp.success) {
            alert("결제 성공!");
        } else {
            alert("결제 실패: " + rsp.error_msg);
        }
    });
}
//$("#productName").val();
//productName 이 부분은 상품명 id값 변경
//금액도 동일