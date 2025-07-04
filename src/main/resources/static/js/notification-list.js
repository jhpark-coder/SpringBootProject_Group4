// 알림 목록 관리
class NotificationList {
    constructor() {
        this.notifications = [];
        this.init();
    }

    init() {
        this.loadNotifications();
        this.createNotificationModal();
        // 전체 읽음 버튼 이벤트 연결
        document.addEventListener('click', (e) => {
            if (e.target && e.target.id === 'markAllReadBtn') {
                this.markAllAsRead();
            }
        });
    }

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

    createNotificationModal() {
        // 알림 목록 모달 HTML 생성
        const modalHTML = `
            <div id="notificationModal" class="notification-modal" style="display: none;">
                <div class="notification-modal-header">
                    <span>알림</span>
                    <button id="markAllReadBtn" class="mark-all-read-btn">전체 읽음</button>
                </div>
                <div class="notification-modal-list" id="notificationList"></div>
            </div>
        `;

        // 종 아이콘(.bell-container) 바로 뒤에 모달 삽입
        const bellContainer = document.querySelector('.bell-container');
        if (bellContainer) {
            bellContainer.insertAdjacentHTML('afterend', modalHTML);
        }
    }

    updateNotificationList() {
        const notificationList = document.getElementById('notificationList');
        if (!notificationList) return;

        if (this.notifications.length === 0) {
            notificationList.innerHTML = '<div style="padding: 20px; text-align: center; color: #666;">알림이 없습니다.</div>';
            return;
        }

        const notificationsHTML = this.notifications.map(notification => {
            const isUnread = !notification.isRead;
            const timeAgo = this.getTimeAgo(notification.createdAt);
            const typeClass = this.getTypeClass(notification.type);
            const typeText = this.getTypeText(notification.type);

            return `
                <div class="notification-item ${isUnread ? 'unread' : ''}" 
                     onclick="notificationList.handleNotificationClick(${notification.id}, '${notification.link}')">
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

    getTypeClass(type) {
        switch (type) {
            case 'seller_approved': return 'seller_approved';
            case 'seller_rejected': return 'seller_rejected';
            case 'seller_request_received': return 'seller_request_received';
            default: return '';
        }
    }

    getTypeText(type) {
        switch (type) {
            case 'seller_approved': return '승인';
            case 'seller_rejected': return '거절';
            case 'seller_request_received': return '신청';
            default: return type;
        }
    }

    getTimeAgo(createdAt) {
        const now = new Date();
        const created = new Date(createdAt);
        const diffMs = now - created;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return '방금 전';
        if (diffMins < 60) return `${diffMins}분 전`;
        if (diffHours < 24) return `${diffHours}시간 전`;
        return `${diffDays}일 전`;
    }

    handleNotificationClick(notificationId, link) {
        // 알림 읽음 처리
        this.markAsRead(notificationId);
        
        // 링크로 이동
        if (link) {
            window.location.href = link;
        }
        
        this.closeModal();
    }

    async markAsRead(notificationId) {
        try {
            await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            
            // 알림 배지 업데이트
            if (window.notificationBadge) {
                window.notificationBadge.decrementBadge();
            }
        } catch (error) {
            console.error('알림 읽음 처리 실패:', error);
        }
    }

    openModal() {
        const modal = document.getElementById('notificationModal');
        if (modal) {
            modal.style.display = 'block';
            this.loadNotifications(); // 모달 열 때 최신 알림 로드
            // 모달 외부 클릭 시 닫기
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

    handleOutsideClick = (event) => {
        const modal = document.getElementById('notificationModal');
        if (modal && !modal.contains(event.target)) {
            this.closeModal();
        }
    }

    async markAllAsRead() {
        try {
            await fetch('/api/notifications/read-all', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });
            // 모든 알림을 읽음 처리
            this.notifications.forEach(n => n.isRead = true);
            this.updateNotificationList();
            if (window.notificationBadge) window.notificationBadge.updateBadge(0);
        } catch (error) {
            console.error('전체 읽음 처리 실패:', error);
        }
    }
}

// 전역 변수로 설정
window.notificationList = new NotificationList();

// 헤더의 알림 벨 클릭 이벤트
document.addEventListener('DOMContentLoaded', function() {
    const bellContainer = document.querySelector('.bell-container');
    if (bellContainer) {
        bellContainer.addEventListener('click', function(e) {
            e.preventDefault();
            window.notificationList.openModal();
        });
    }
}); 