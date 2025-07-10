// 알림 목록 관리
class NotificationList {
    constructor() {
        this.notifications = [];
        this.filteredCategory = 'all'; // 추가: 현재 필터 카테고리
        this.csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
        this.csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
        this.init(); // init 호출을 다시 활성화
    }

    // init 메서드를 다시 활성화하여 '전체 읽음' 버튼 이벤트를 연결
    init() {
        // '전체 읽음' 버튼에 대한 이벤트 리스너
        const markAllReadBtn = document.getElementById('markAllReadBtn');
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.markAllAsRead();
            });
        }

        // 필터 버튼 이벤트 리스너 등록
        document.addEventListener('DOMContentLoaded', () => {
            const filterBar = document.querySelector('.notification-filter-bar');
            if (filterBar) {
                filterBar.querySelectorAll('.filter-btn').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        const category = btn.getAttribute('data-category');
                        this.filteredCategory = category;
                        // 버튼 활성화 스타일 처리
                        filterBar.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                        btn.classList.add('active');
                        this.updateNotificationList();
                    });
                });
                // 기본값: 전체 활성화
                filterBar.querySelector('.filter-btn[data-category="all"]').classList.add('active');
            }
        });
    }

    // createNotificationModal 메서드도 더 이상 필요하지 않습니다.
    /*
    createNotificationModal() {
        // 알림 목록 모달 HTML 생성 (카테고리 탭 없는 버전)
        const modalHTML = `
            <div id="notificationModal" class="notification-modal" style="display: none;">
                <div class="notification-modal-header">
                    <span>알림</span>
                    <button id="markAllReadBtn" class="mark-all-read-btn">전체 읽음</button>
                </div>
                <div class="notification-modal-list" id="notificationList"></div>
            </div>
        `;

        // 종 아이콘(.bell-container)의 자식으로 모달 삽입
        const bellContainer = document.querySelector('.bell-container');
        if (bellContainer) {
            bellContainer.insertAdjacentHTML('beforeend', modalHTML);
        }
    }
    */

    async loadNotifications() {
        try {
            const response = await fetch('/api/notifications/list', {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                const data = await response.json();
                this.notifications = data.notifications || [];
                this.updateNotificationList();
            }
        } catch (error) {
            console.error('알림 목록 조회 실패:', error);
        }
    }

    updateNotificationList() {
        const notificationList = document.getElementById('notificationList');
        if (!notificationList) return;

        // 카테고리별 필터링
        let filtered = this.notifications;
        switch (this.filteredCategory) {
            case 'social':
                filtered = this.notifications.filter(n => ['follow', 'like', 'comment'].includes(n.type));
                break;
            case 'product':
                filtered = this.notifications.filter(n => ['product_inquiry', 'product_review'].includes(n.type));
                break;
            case 'admin':
                filtered = this.notifications.filter(n => [
                    'seller_request_received', 'seller_request_submitted', 'seller_approved', 'seller_rejected', 'admin_notice'
                ].includes(n.type));
                break;
            case 'auction':
                filtered = this.notifications.filter(n => ['auction_bid', 'auction_win', 'auction_lose'].includes(n.type));
                break;
            case 'all':
            default:
                filtered = this.notifications;
        }

        if (filtered.length === 0) {
            notificationList.innerHTML = '<div style="padding: 20px; text-align: center; color: #666;">알림이 없습니다.</div>';
            return;
        }

        const notificationsHTML = filtered.map(notification => {
            const isUnread = !notification.isRead;
            const timeAgo = this.getTimeAgo(notification.createdAt);
            const typeClass = this.getTypeClass(notification.type);
            const typeText = this.getTypeText(notification.type);

            return `
                <div class="notification-item ${isUnread ? 'unread' : ''}" 
                     onclick="notificationList.handleNotificationClick(${notification.id}, '${notification.link}', '${notification.type}')">
                    <div class="notification-message">${notification.message}</div>
                    <div class="notification-time">
                        ${timeAgo}
                        <span class="notification-type ${typeClass}">${typeText}</span>
                    </div>
                </div>
            `;
        }).join('');

        notificationList.innerHTML = notificationsHTML;
    }

    getTimeAgo(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const seconds = Math.floor((now - date) / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);

        if (days > 7) {
            return date.toLocaleDateString();
        } else if (days > 0) {
            return `${days}일 전`;
        } else if (hours > 0) {
            return `${hours}시간 전`;
        } else if (minutes > 0) {
            return `${minutes}분 전`;
        } else {
            return '방금 전';
        }
    }

    getTypeClass(type) {
        switch (type) {
            case 'follow': return 'type-follow';
            case 'like': return 'type-like';
            case 'comment': return 'type-comment';
            case 'seller_request_received':
            case 'seller_request_submitted':
            case 'seller_approved':
            case 'seller_rejected':
                return 'type-seller';
            default: return 'type-default';
        }
    }

    getTypeText(type) {
        switch (type) {
            case 'follow': return '팔로우';
            case 'like': return '좋아요';
            case 'comment': return '댓글';
            case 'seller_request_received': return '작가신청';
            case 'seller_request_submitted': return '작가신청';
            case 'seller_approved': return '승인';
            case 'seller_rejected': return '거절';
            default: return '알림';
        }
    }

    async markAsRead(notificationId) {
        try {
            const headers = { 'Content-Type': 'application/json' };
            if (this.csrfToken && this.csrfHeader) {
                headers[this.csrfHeader] = this.csrfToken;
            }

            const response = await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'POST',
                credentials: 'include',
                headers: headers
            });

            if (response.ok) {
                const notification = this.notifications.find(n => n.id === notificationId);
                if (notification) {
                    notification.isRead = true;
                    this.updateNotificationList();
                    if (window.notificationBadge) window.notificationBadge.decrementBadge();
                }
                return true; // 성공 시 true 반환
            } else {
                console.error('알림 읽음 처리 서버 응답 오류:', response.status);
                return false; // 실패 시 false 반환
            }
        } catch (error) {
            console.error('알림 읽음 처리 실패:', error);
            return false; // 실패 시 false 반환
        }
    }

    async markAllAsRead() {
        try {
            const headers = { 'Content-Type': 'application/json' };
            if (this.csrfToken && this.csrfHeader) {
                headers[this.csrfHeader] = this.csrfToken;
            }

            await fetch('/api/notifications/read-all', {
                method: 'POST',
                credentials: 'include',
                headers: headers
            });
            this.notifications.forEach(n => n.isRead = true);
            this.updateNotificationList();
            if (window.notificationBadge) window.notificationBadge.updateBadge(0);
        } catch (error) {
            console.error('전체 읽음 처리 실패:', error);
        }
    }

    async handleNotificationClick(notificationId, link, type) {
        // '작가 승인' 알림에 대한 특별 처리
        if (type === 'seller_approved') {
            const isSuccess = await this.markAsRead(notificationId);
            if (isSuccess) {
                // 기존 토스트 알림 함수 사용
                if (window.notificationBadge && typeof window.notificationBadge.showNotificationToast === 'function') {
                    window.notificationBadge.showNotificationToast({
                        message: '권한이 변경되었습니다. 다시 로그인해주세요.'
                    });
                }
                setTimeout(() => {
                    sessionStorage.setItem('post_logout_redirect', '/members/login');
                    sessionStorage.setItem('post_logout_message', '작가 등급이 승인되었습니다. 변경된 권한을 적용하기 위해 다시 로그인해주세요.');
                    window.location.href = '/members/logout';
                }, 1500);
            }
            return; // 페이지 이동을 막기 위해 여기서 함수 종료
        }

        // 서버가 읽음 처리를 성공적으로 완료했는지 확인합니다.
        const isSuccess = await this.markAsRead(notificationId);

        // 성공한 경우에만 페이지를 이동합니다.
        if (
            isSuccess &&
            link &&
            link !== 'null' &&
            link !== 'undefined' &&
            link !== ''
        ) {
            window.location.href = link;
        }

        // 모달은 성공 여부와 관계없이 닫습니다.
        this.closeModal();
    }

    toggleModal() {
        console.log('🔔 notificationList.toggleModal() 호출됨');
        const modal = document.getElementById('notificationModal');
        if (modal) {
            const isModalOpen = modal.style.display === 'flex';
            if (isModalOpen) {
                this.closeModal();
            } else {
                this.openModal();
            }
        }
    }

    openModal() {
        const modal = document.getElementById('notificationModal');
        if (modal) {
            modal.style.display = 'flex';
            this.loadNotifications();
            setTimeout(() => {
                document.addEventListener('mousedown', this.handleOutsideClick);
            }, 0);
        }
    }

    closeModal() {
        const modal = document.getElementById('notificationModal');
        if (modal) {
            modal.style.display = 'none';
            document.removeEventListener('mousedown', this.handleOutsideClick);
        }
    }

    handleOutsideClick = (e) => {
        const modal = document.getElementById('notificationModal');
        const bellContainer = document.querySelector('.bell-container');
        // bell-container나 그 자식 요소를 클릭한 경우는 무시
        if (modal && !modal.contains(e.target) && bellContainer && !bellContainer.contains(e.target)) {
            this.closeModal();
        }
    }
}

// 전역 변수로 설정
window.notificationList = new NotificationList();