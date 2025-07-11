// 구독 기능 JavaScript

// 구독 토글 함수 (사용자 ID 기반, 버튼 엘리먼트 직접 전달)
function toggleFollowOriginal(userId, clickedBtn) {
    console.log('toggleFollow 함수 호출됨:', userId, clickedBtn);

    // clickedBtn이 없으면 이벤트에서 가져오기
    if (!clickedBtn && window.event) {
        clickedBtn = window.event.target;
        console.log('이벤트에서 버튼 가져옴:', clickedBtn);
    }

    if (!clickedBtn) {
        console.log('clickedBtn이 없음');
        return;
    }

    // 이벤트 전파 중단 (투명 링크 클릭 방지)
    if (window.event) {
        window.event.stopPropagation();
        window.event.preventDefault();
        console.log('이벤트 전파 중단됨');
    }

    // 중복 클릭 방지
    clickedBtn.disabled = true;

    // 페이지 내 동일 작가 ID 버튼 모두 가져오기
    const allButtons = document.querySelectorAll(`[data-member-id="${userId}"]`);

    // Optimistic UI
    const isUnfollowing = clickedBtn.classList.contains('following');
    allButtons.forEach(btn => {
        if (isUnfollowing) {
            btn.classList.remove('following');
            btn.textContent = 'Follow';
        } else {
            btn.classList.add('following');
            btn.textContent = 'Following';
        }
    });

    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    console.log('CSRF 토큰:', csrfToken);
    console.log('CSRF 헤더:', csrfHeader);

    // 헤더 설정
    const headers = {
        'Content-Type': 'application/json',
    };

    // CSRF 토큰이 있으면 헤더에 추가
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
        console.log('CSRF 토큰이 헤더에 추가됨');
    } else {
        console.log('CSRF 토큰이 없음');
    }

    console.log('요청 헤더:', headers);
    console.log('요청 URL:', `/api/follow/${userId}`);

    // 현재 사용자 정보 확인
    console.log('현재 페이지 URL:', window.location.href);
    console.log('로그인 상태 확인:', document.querySelector('[data-login-status]')?.getAttribute('data-login-status'));

    // 현재 사용자 정보 확인 (Thymeleaf에서 설정된 전역 변수)
    console.log('window.currentUser:', window.currentUser);
    console.log('사용자 ID:', window.currentUser?.id);

    // 로그인되지 않은 경우 처리
    if (!window.currentUser || !window.currentUser.id) {
        console.log('로그인되지 않은 사용자입니다.');
        alert('팔로우 기능을 사용하려면 로그인이 필요합니다.');
        window.location.href = '/members/login?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
        return;
    }

    fetch(`/api/follow/${userId}`, {
        method: 'POST',
        headers: headers
    })
        .then(response => {
            console.log('서버 응답 상태:', response.status);
            console.log('서버 응답 헤더:', response.headers);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return response.text().then(text => {
                console.log('서버 응답 텍스트:', text);
                try {
                    return JSON.parse(text);
                } catch (e) {
                    console.error('JSON 파싱 오류:', e);
                    console.error('응답 텍스트:', text);
                    throw new Error('서버에서 잘못된 응답을 받았습니다.');
                }
            });
        })
        .then(data => {
            if (data.error) {
                // 실패 시 원래대로 복구
                allButtons.forEach(btn => {
                    if (isUnfollowing) {
                        btn.classList.add('following');
                        btn.textContent = 'Following';
                    } else {
                        btn.classList.remove('following');
                        btn.textContent = 'Follow';
                    }
                });
                alert(data.message);
                return;
            }
            // 성공 시 최종 상태 반영
            allButtons.forEach(btn => {
                if (data.isFollowing) {
                    btn.classList.add('following');
                    btn.textContent = 'Following';
                } else {
                    btn.classList.remove('following');
                    btn.textContent = 'Follow';
                }
            });
            // 팔로워 수 갱신
            const followerCount = document.querySelector(`[data-follower-count="${userId}"]`);
            if (followerCount) {
                followerCount.textContent = data.followerCount;
            }
        })
        .catch(error => {
            console.error('팔로우 오류:', error);
            alert('팔로우 기능에 문제가 발생했습니다.');
            // 실패 복구
            allButtons.forEach(btn => {
                if (isUnfollowing) {
                    btn.classList.add('following');
                    btn.textContent = 'Following';
                } else {
                    btn.classList.remove('following');
                    btn.textContent = 'Follow';
                }
            });
        })
        .finally(() => {
            clickedBtn.disabled = false;
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
    console.log('DOM 로드 완료, 팔로우 버튼 이벤트 리스너 추가');

    // 팔로우 버튼에 이벤트 리스너 추가 (onclick 속성이 없는 버튼만)
    const followButtons = document.querySelectorAll('.follow-btn, .btn.follow-btn');
    followButtons.forEach(button => {
        // onclick 속성이 이미 있는 버튼은 건너뛰기 (중복 방지)
        if (button.hasAttribute('onclick')) {
            console.log('팔로우 버튼 (onclick 있음, 건너뜀):', button);
            return;
        }

        console.log('팔로우 버튼 발견 (이벤트 리스너 추가):', button);
        button.addEventListener('click', function (e) {
            console.log('팔로우 버튼 클릭됨:', this);
            e.stopPropagation();
            e.preventDefault();

            const userId = this.getAttribute('data-member-id');
            if (userId) {
                console.log('userId:', userId);
                toggleFollow(userId, this);
            }
        });
    });

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
window.toggleFollowOriginal = toggleFollowOriginal;
window.toggleFollow = toggleFollowOriginal; // 호환성을 위해 유지
window.toggleLike = toggleLike;
window.loadFollowStats = loadFollowStats;
window.requestPointPay = requestPointPay;
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

    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // 헤더 설정
    const headers = {
        'Content-Type': 'application/json',
    };

    // CSRF 토큰이 있으면 헤더에 추가
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    // 포인트 결제 API 호출
    fetch(`/api/products/${productId}/purchase/point`, {
        method: 'POST',
        headers: headers,
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
                        // 원래 상품 ID를 URL 파라미터로 전달
                        const returnProductId = window.location.pathname.split('/').pop();
                        window.location.href = `/members/point?returnProductId=${returnProductId}`;
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

// 기존 requestPay 함수 (호환성을 위해 유지)
function requestPay() {
    IMP.init("imp20067661"); // 실제 가맹점 코드로 바꿔주세요

    IMP.request_pay({
        pg: "html5_inicis.INIpayTest",         // 결제 대행사
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