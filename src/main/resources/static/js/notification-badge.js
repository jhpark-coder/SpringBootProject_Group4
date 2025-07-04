// 알림 배지 관리
class NotificationBadge {
    constructor() {
        this.badge = document.getElementById('notification-badge');
        this.updateInterval = null;
        this.init();
    }

    init() {
        if (this.badge) {
            this.updateNotificationCount();
            // 5초마다 알림 개수 업데이트
            this.updateInterval = setInterval(() => {
                this.updateNotificationCount();
            }, 5000);
        }
    }

    async updateNotificationCount() {
        try {
            const response = await fetch('/api/notifications/count', {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                const data = await response.json();
                const count = data.count || 0;
                this.updateBadge(count);
            }
        } catch (error) {
            console.error('알림 개수 조회 실패:', error);
        }
    }

    updateBadge(count) {
        if (!this.badge) return;

        if (count > 0) {
            this.badge.textContent = count > 99 ? '99+' : count.toString();
            this.badge.style.display = 'flex';
        } else {
            this.badge.style.display = 'none';
        }
    }

    // 실시간 알림 수신 시 배지 업데이트
    incrementBadge() {
        if (!this.badge) return;

        const currentCount = parseInt(this.badge.textContent) || 0;
        this.updateBadge(currentCount + 1);
    }

    // 알림 읽음 처리 시 배지 감소
    decrementBadge() {
        if (!this.badge) return;

        const currentCount = parseInt(this.badge.textContent) || 0;
        if (currentCount > 0) {
            this.updateBadge(currentCount - 1);
        }
    }

    destroy() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
        }
    }
}

// 페이지 로드 시 알림 배지 초기화
document.addEventListener('DOMContentLoaded', function() {
    window.notificationBadge = new NotificationBadge();

    const notificationBell = document.getElementById('notification-bell-anchor');
    const notificationModal = document.getElementById('notificationModal');
    let allNotifications = []; // 모든 알림을 저장할 배열
    let currentCategory = 'ALL'; // 현재 선택된 카테고리

    if (!notificationBell || !notificationModal) {
        console.warn('알림 벨 또는 모달을 찾을 수 없습니다.');
        return;
    }

    // 알림 벨 클릭 시 모달 토글
    notificationBell.addEventListener('click', function(event) {
        event.preventDefault();
        event.stopPropagation();
        const isVisible = notificationModal.style.display === 'flex';
        notificationModal.style.display = isVisible ? 'none' : 'flex';
        if (!isVisible) {
            loadAndRenderNotifications();
        }
    });

    // 모달 외부 클릭 시 닫기
    document.addEventListener('click', function(event) {
        if (!notificationModal.contains(event.target) && !notificationBell.contains(event.target)) {
            notificationModal.style.display = 'none';
        }
    });

    // 아이콘 매핑
    const categoryIcons = {
        'SOCIAL': '<i class="fas fa-user-friends"></i>',
        'AUCTION': '<i class="fas fa-gavel"></i>',
        'ORDER': '<i class="fas fa-receipt"></i>',
        'ADMIN': '<i class="fas fa-user-shield"></i>',
        'DEFAULT': '<i class="fas fa-bell"></i>'
    };

    // 알림 목록 렌더링 함수
    function renderNotifications(category = 'ALL') {
        currentCategory = category;
        const listContainer = notificationModal.querySelector('.notification-modal-list');
        if (!listContainer) return;

        const filteredNotifications = category === 'ALL'
            ? allNotifications
            : allNotifications.filter(n => n.category === category);

        if (filteredNotifications.length > 0) {
            let notificationListHtml = '';
            filteredNotifications.forEach(notification => {
                const icon = categoryIcons[notification.category] || categoryIcons['DEFAULT'];
                notificationListHtml += `
                    <div class="notification-item ${notification.isRead ? '' : 'unread'}" data-id="${notification.id}" data-link="${notification.link}">
                        <div class="notification-item-icon"><div class="icon-placeholder">${icon}</div></div>
                        <div class="notification-item-content">
                            <p class="notification-item-message">${notification.message}</p>
                            <div class="notification-item-time">${new Date(notification.createdAt).toLocaleString()}</div>
                        </div>
                    </div>`;
            });
            listContainer.innerHTML = notificationListHtml;
        } else {
            listContainer.innerHTML = '<p style="padding: 15px; text-align: center;">이 카테고리에는 알림이 없습니다.</p>';
        }

        document.querySelectorAll('.notification-tab').forEach(tab => {
            tab.classList.toggle('active', tab.dataset.category === category);
        });
    }

    // 알림 데이터 로드 및 초기 렌더링
    async function loadAndRenderNotifications() {
        notificationModal.innerHTML = `
            <div class="notification-modal-header">
                <h5>알림</h5>
                <button class="notification-settings-btn" title="모두 읽음"><i class="fas fa-check-double"></i></button>
            </div>
            <div class="notification-modal-tabs">
                <button class="notification-tab active" data-category="ALL">전체</button>
                <button class="notification-tab" data-category="SOCIAL">소셜</button>
                <button class="notification-tab" data-category="AUCTION">경매</button>
                <button class="notification-tab" data-category="ORDER">주문</button>
                <button class="notification-tab" data-category="ADMIN">관리</button>
            </div>
            <div class="notification-modal-list"><p style="padding: 15px; text-align: center;">로딩 중...</p></div>`;
        
        const listContainer = notificationModal.querySelector('.notification-modal-list');
        try {
            const response = await fetch('/api/notifications/list', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const data = await response.json();
                allNotifications = data.notifications || [];
                renderNotifications(currentCategory);
            } else {
                 listContainer.innerHTML = '<p style="padding: 15px; text-align: center; color: red;">알림을 불러오는데 실패했습니다.</p>';
            }
        } catch(error) {
            console.error("알림 목록 로딩 실패:", error);
            listContainer.innerHTML = '<p style="padding: 15px; text-align: center; color: red;">알림을 불러오는데 실패했습니다.</p>';
        }
    }

    // 이벤트 위임
    notificationModal.addEventListener('click', async function(event) {
        const item = event.target.closest('.notification-item');
        const tab = event.target.closest('.notification-tab');
        const settingsBtn = event.target.closest('.notification-settings-btn');
        
        const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
        const csrfToken = csrfTokenEl ? csrfTokenEl.content : null;
        const csrfHeader = csrfHeaderEl ? csrfHeaderEl.content : null;

        if (tab) {
            renderNotifications(tab.dataset.category);
            return;
        }

        if (item) {
            const notificationId = item.dataset.id;
            const link = item.dataset.link;
            try {
                if(csrfToken && csrfHeader) {
                    const headers = { 'Content-Type': 'application/json' };
                    headers[csrfHeader] = csrfToken;
                    await fetch(`/api/notifications/${notificationId}/read`, { method: 'POST', credentials: 'include', headers });
                }
                if (link && link !== 'null') {
                    window.location.href = link;
                } else {
                    loadAndRenderNotifications();
                    window.notificationBadge.updateNotificationCount();
                }
            } catch(error) {
                console.error("알림 읽음 처리 실패:", error);
                if (link && link !== 'null') window.location.href = link;
            }
        }

        if (settingsBtn) {
            try {
                if(csrfToken && csrfHeader) {
                    const headers = { 'Content-Type': 'application/json' };
                    headers[csrfHeader] = csrfToken;
                    const response = await fetch('/api/notifications/read-all', { method: 'POST', credentials: 'include', headers });
                    if(response.ok) {
                        loadAndRenderNotifications();
                        window.notificationBadge.updateNotificationCount();
                    } else {
                        console.error('모두 읽음 처리 실패:', response.statusText);
                    }
                }
            } catch (error) {
                console.error('모두 읽음 처리 중 오류 발생:', error);
            }
        }
    });
});

// WebSocket을 통한 실시간 알림 수신
// (알림용 WebSocket 코드 전체 삭제)

// 현재 사용자 ID를 가져오는 함수
function getCurrentUserId() {
    const bellContainer = document.querySelector('.bell-container');
    return bellContainer ? bellContainer.getAttribute('data-user-id') : null;
} 