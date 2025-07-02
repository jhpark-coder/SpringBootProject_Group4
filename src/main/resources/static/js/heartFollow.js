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
window.requestPointPay = requestPointPay;
window.requestSubscription = requestSubscription;
window.requestPay = requestPay;

// 포인트 결제 함수
function requestPointPay() {
    // 현재 페이지의 상품 정보 가져오기
    const productId = window.location.pathname.split('/').pop();
    const productName = document.querySelector('.artwork-main h1').textContent;
    const priceElement = document.querySelector('.purchase-btn');
    const priceText = priceElement.textContent;
    
    // 가격 추출 (숫자만)
    const priceMatch = priceText.match(/(\d+)P/);
    const price = priceMatch ? parseInt(priceMatch[1]) : 0;
    
    if (price === 0) {
        alert('상품 가격을 확인할 수 없습니다.');
        return;
    }
    
    // 사용자 확인
    if (!confirm(`${price}P로 이 상품을 구매하시겠습니까?`)) {
        return;
    }
    
    // 포인트 결제 API 호출
    fetch(`/api/products/${productId}/purchase/point`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            productId: productId,
            price: price
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('포인트 결제가 완료되었습니다!');
            // 페이지 새로고침 또는 구매 완료 페이지로 이동
            window.location.reload();
        } else {
            // 포인트 부족 시 포인트 충전 페이지로 이동
            if (data.message && data.message.includes('포인트가 부족')) {
                if (confirm('포인트가 부족합니다. 포인트 충전 페이지로 이동하시겠습니까?')) {
                    window.location.href = '/members/point';
                }
            } else {
                alert('포인트 결제 실패: ' + data.message);
            }
        }
    })
    .catch(error => {
        console.error('포인트 결제 오류:', error);
        alert('포인트 결제 중 오류가 발생했습니다.');
    });

}

// 정기구독 함수
function requestSubscription() {
    // 현재 페이지의 상품 정보 가져오기
    const productId = window.location.pathname.split('/').pop();
    const productName = document.querySelector('.artwork-main h1').textContent;
    const authorName = document.querySelector('.author-name').textContent;
    
    // 사용자 확인
    if (!confirm(`${authorName} 작가님의 정기구독을 신청하시겠습니까?\n\n정기구독 혜택:\n- 해당 작가의 모든 작품을 무제한으로 볼 수 있습니다\n- 월 9,900원 (첫 달 무료)\n- 언제든지 해지 가능`)) {
        return;
    }
    
    // 정기구독 API 호출
    fetch(`/api/products/${productId}/subscription`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            productId: productId,
            authorId: getAuthorIdFromPage()
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('정기구독 신청이 완료되었습니다!\n\n첫 달은 무료로 이용하실 수 있으며, 다음 달부터 월 9,900원이 자동으로 결제됩니다.');
            // 구독 버튼 상태 변경
            updateSubscriptionButton(true);
        } else {
            alert('정기구독 신청 실패: ' + data.message);
        }
    })
    .catch(error => {
        console.error('정기구독 오류:', error);
        alert('정기구독 신청 중 오류가 발생했습니다.');
    });
}

// 작가 ID를 페이지에서 추출하는 함수
function getAuthorIdFromPage() {
    const authorElement = document.querySelector('.author-name');
    if (authorElement && authorElement.dataset.authorId) {
        return authorElement.dataset.authorId;
    }
    return null;
}

// 구독 버튼 상태 업데이트 함수
function updateSubscriptionButton(isSubscribed) {
    const subscriptionBtn = document.querySelector('.subscription-btn');
    if (subscriptionBtn) {
        if (isSubscribed) {
            subscriptionBtn.textContent = '구독 중';
            subscriptionBtn.style.background = '#28a745';
            subscriptionBtn.onclick = function() {
                alert('이미 구독 중인 작가입니다.');
            };
        }
    }
}

// 기존 requestPay 함수 (호환성을 위해 유지)
function requestPay() {
    IMP.init("imp20067661"); // 실제 가맹점 코드로 바꿔주세요

    IMP.request_pay({
        pg: "html5_inicis",         // 결제 대행사
        pay_method: "card",         // 결제 수단
        merchantuid: "order" + new Date().getTime(), // 주문번호 (고유해야 함)
        name: "",
        amount: 1000                // 결제 금액 (원 단위)
    }, function (rsp) {
        if (rsp.success) {
            alert("결제 성공!");
            console.log(rsp);
        } else {
            alert("결제 실패: " + rsp.error_msg);
        }
    });
}
function requestPubPay() {
    IMP.init("imp20067661"); // 실제 가맹점 코드로 바꿔주세요

    IMP.request_pay({
        pg: "html5_inicis",         // 결제 대행사
        pay_method: "card",         // 결제 수단
        merchantuid: "order" + new Date().getTime(), // 주문번호 (고유해야 함)
        name: "구독권",
        amount: 1000                // 결제 금액 (원 단위)
    }, function (rsp) {
        if (rsp.success) {
            alert("결제 성공!");
            console.log(rsp);
        } else {
            alert("결제 실패: " + rsp.error_msg);
        }
    });
}

// SweetAlert2 예시
Swal.fire({
  title: '구독 결제',
  html: `월 구독료: <b>9,900원</b><br>결제하시겠습니까?`,
  showCancelButton: true,
  confirmButtonText: '결제하기',
  cancelButtonText: '취소'
}).then((result) => {
  if (result.isConfirmed) {
    // 결제 API 호출
    fetch('/api/subscription', { ... })
      .then(...)
  }
});