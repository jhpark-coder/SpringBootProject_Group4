// 구독 관련 JavaScript

// SweetAlert2 라이브러리 사용 (CDN에서 로드 필요)
// <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

/**
 * 구독 결제 모달 표시
 */
function showSubscriptionModal(productId, authorId, authorName, monthlyPrice) {
    Swal.fire({
        title: '구독 결제',
        html: `
            <div style="text-align: left; margin: 20px 0;">
                <h4>${authorName} 작가님 구독</h4>
                <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0;">
                    <p><strong>구독 혜택:</strong></p>
                    <ul style="margin: 10px 0; padding-left: 20px;">
                        <li>해당 작가의 모든 작품을 무제한으로 볼 수 있습니다</li>
                        <li>월 ${monthlyPrice.toLocaleString()}원 (첫 달 무료)</li>
                        <li>언제든지 해지 가능</li>
                    </ul>
                </div>
                <div style="background: #e3f2fd; padding: 15px; border-radius: 8px; margin: 15px 0;">
                    <p><strong>결제 정보:</strong></p>
                    <p>월 구독료: <span style="color: #1976d2; font-weight: bold;">${monthlyPrice.toLocaleString()}원</span></p>
                    <p style="color: #666; font-size: 14px;">* 첫 달은 무료로 이용하실 수 있습니다</p>
                </div>
            </div>
        `,
        icon: 'info',
        showCancelButton: true,
        confirmButtonColor: '#1976d2',
        cancelButtonColor: '#6c757d',
        confirmButtonText: '구독하기',
        cancelButtonText: '취소',
        width: '500px',
        customClass: {
            popup: 'subscription-modal'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            // 구독 API 호출
            createSubscription(productId, authorId, monthlyPrice);
        }
    });
}

/**
 * 구독 API 호출
 */
function createSubscription(productId, authorId, monthlyPrice) {
    // 로딩 표시
    Swal.fire({
        title: '구독 처리 중...',
        text: '잠시만 기다려주세요.',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // API 요청 데이터
    const requestData = {
        productId: productId,
        authorId: authorId,
        monthlyPrice: monthlyPrice,
        paymentMethod: 'POINT',
        autoRenewal: true
    };

    // 구독 API 호출
    fetch('/api/subscriptions', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 구독 성공
            Swal.fire({
                title: '구독 완료!',
                html: `
                    <div style="text-align: center;">
                        <p>구독이 성공적으로 시작되었습니다.</p>
                        <p style="color: #666; font-size: 14px;">마이페이지에서 구독 상태를 확인할 수 있습니다.</p>
                    </div>
                `,
                icon: 'success',
                confirmButtonText: '확인'
            }).then(() => {
                // 페이지 새로고침 또는 리다이렉트
                window.location.reload();
            });
        } else {
            // 구독 실패
            Swal.fire({
                title: '구독 실패',
                text: data.message || '구독 처리 중 오류가 발생했습니다.',
                icon: 'error',
                confirmButtonText: '확인'
            });
        }
    })
    .catch(error => {
        console.error('구독 API 오류:', error);
        Swal.fire({
            title: '오류 발생',
            text: '네트워크 오류가 발생했습니다. 다시 시도해주세요.',
            icon: 'error',
            confirmButtonText: '확인'
        });
    });
}

/**
 * 구독 취소 모달 표시
 */
function showCancelSubscriptionModal(subscriptionId, authorName) {
    Swal.fire({
        title: '구독 취소',
        html: `
            <div style="text-align: left; margin: 20px 0;">
                <p><strong>${authorName} 작가님 구독을 취소하시겠습니까?</strong></p>
                <div style="background: #fff3cd; padding: 15px; border-radius: 8px; margin: 15px 0;">
                    <p style="color: #856404; margin: 0;">
                        <strong>주의사항:</strong><br>
                        • 구독 취소 시 즉시 모든 혜택이 중단됩니다<br>
                        • 이미 결제된 금액은 환불되지 않습니다<br>
                        • 언제든지 다시 구독할 수 있습니다
                    </p>
                </div>
                <div>
                    <label for="cancelReason" style="display: block; margin-bottom: 10px; font-weight: bold;">
                        취소 사유 (선택사항):
                    </label>
                    <select id="cancelReason" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                        <option value="">사유를 선택해주세요</option>
                        <option value="가격이 비쌉니다">가격이 비쌉니다</option>
                        <option value="콘텐츠가 마음에 들지 않습니다">콘텐츠가 마음에 들지 않습니다</option>
                        <option value="잠시 이용을 중단합니다">잠시 이용을 중단합니다</option>
                        <option value="다른 서비스를 이용합니다">다른 서비스를 이용합니다</option>
                        <option value="기타">기타</option>
                    </select>
                </div>
            </div>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d',
        confirmButtonText: '구독 취소',
        cancelButtonText: '취소',
        width: '500px',
        preConfirm: () => {
            const reason = document.getElementById('cancelReason').value;
            return reason;
        }
    }).then((result) => {
        if (result.isConfirmed) {
            // 구독 취소 API 호출
            cancelSubscription(subscriptionId, result.value);
        }
    });
}

/**
 * 구독 취소 API 호출
 */
function cancelSubscription(subscriptionId, reason) {
    // 로딩 표시
    Swal.fire({
        title: '구독 취소 처리 중...',
        text: '잠시만 기다려주세요.',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // API 요청 데이터
    const requestData = {
        reason: reason || '사용자 요청'
    };

    // 구독 취소 API 호출
    fetch(`/api/subscriptions/${subscriptionId}/cancel`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 구독 취소 성공
            Swal.fire({
                title: '구독 취소 완료',
                text: '구독이 성공적으로 취소되었습니다.',
                icon: 'success',
                confirmButtonText: '확인'
            }).then(() => {
                // 페이지 새로고침
                window.location.reload();
            });
        } else {
            // 구독 취소 실패
            Swal.fire({
                title: '구독 취소 실패',
                text: data.message || '구독 취소 처리 중 오류가 발생했습니다.',
                icon: 'error',
                confirmButtonText: '확인'
            });
        }
    })
    .catch(error => {
        console.error('구독 취소 API 오류:', error);
        Swal.fire({
            title: '오류 발생',
            text: '네트워크 오류가 발생했습니다. 다시 시도해주세요.',
            icon: 'error',
            confirmButtonText: '확인'
        });
    });
}

/**
 * 내 구독 목록 조회
 */
function loadMySubscriptions(page = 0) {
    fetch(`/api/subscriptions/my?page=${page}&size=10`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displaySubscriptions(data.data);
            } else {
                console.error('구독 목록 조회 실패:', data.message);
            }
        })
        .catch(error => {
            console.error('구독 목록 조회 오류:', error);
        });
}

/**
 * 구독 목록 표시
 */
function displaySubscriptions(subscriptionData) {
    const container = document.getElementById('subscription-list');
    if (!container) return;

    const subscriptions = subscriptionData.content;
    
    if (subscriptions.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #666; padding: 40px;">구독 중인 작가가 없습니다.</p>';
        return;
    }

    let html = '';
    subscriptions.forEach(subscription => {
        const statusClass = getStatusClass(subscription.status);
        const statusText = getStatusText(subscription.status);
        
        html += `
            <div class="subscription-item" style="border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin-bottom: 15px;">
                <div style="display: flex; justify-content: between; align-items: center;">
                    <div style="flex: 1;">
                        <h4 style="margin: 0 0 10px 0;">${subscription.authorName} 작가님</h4>
                        <p style="margin: 5px 0; color: #666;">상품: ${subscription.productName}</p>
                        <p style="margin: 5px 0; color: #666;">월 구독료: ${subscription.monthlyPrice.toLocaleString()}원</p>
                        <p style="margin: 5px 0; color: #666;">구독 기간: ${formatDate(subscription.startDate)} ~ ${formatDate(subscription.endDate)}</p>
                        <span class="status-badge ${statusClass}" style="padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold;">
                            ${statusText}
                        </span>
                    </div>
                    <div style="text-align: right;">
                        ${subscription.status === 'ACTIVE' ? 
                            `<button onclick="showCancelSubscriptionModal(${subscription.subscriptionId}, '${subscription.authorName}')" 
                                     style="background: #dc3545; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
                                구독 취소
                            </button>` : 
                            ''
                        }
                    </div>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

/**
 * 상태별 CSS 클래스 반환
 */
function getStatusClass(status) {
    switch (status) {
        case 'ACTIVE': return 'status-active';
        case 'EXPIRED': return 'status-expired';
        case 'CANCELLED': return 'status-cancelled';
        default: return 'status-default';
    }
}

/**
 * 상태별 텍스트 반환
 */
function getStatusText(status) {
    switch (status) {
        case 'ACTIVE': return '구독 중';
        case 'EXPIRED': return '만료됨';
        case 'CANCELLED': return '취소됨';
        default: return '알 수 없음';
    }
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}

// 전역 함수로 등록
window.showSubscriptionModal = showSubscriptionModal;
window.showCancelSubscriptionModal = showCancelSubscriptionModal;
window.loadMySubscriptions = loadMySubscriptions; 