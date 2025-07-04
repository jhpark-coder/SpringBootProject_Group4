// 알림 배지 관리
class NotificationBadge {
    constructor() {
        this.badge = document.getElementById('notification-badge');
        this.socket = null;
        this.initialized = false;
        // DOMContentLoaded 이후에 초기화
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.init());
        } else {
            this.init();
        }
    }

    init() {
        if (this.badge && !this.initialized) {
            try {
                const bellContainer = document.querySelector('.bell-container');
                const userId = bellContainer ? parseInt(bellContainer.dataset.userId) : null;

                if (!userId) {
                    console.warn('사용자 ID를 찾을 수 없습니다.');
                    return;
                }

                // Socket.IO가 로드될 때까지 대기
                const initSocket = () => {
                    if (typeof io !== 'undefined') {
                        // Socket.IO 연결 설정
                        this.socket = io('http://localhost:3000', {
                            withCredentials: true,
                            transports: ['websocket'],
                            autoConnect: true,
                            reconnection: true,
                            reconnectionAttempts: 5,
                            reconnectionDelay: 1000,
                            auth: {
                                userId: userId,
                                roles: window.currentUser ? window.currentUser.roles : []
                            }
                        });

                        // 연결 이벤트 핸들러
                        this.socket.on('connect', () => {
                            console.log('WebSocket 연결 성공');
                            this.initialized = true;
                            // 초기 알림 목록 및 개수 요청
                            this.socket.emit('findAllNotifications');
                            
                            // 'notifications' 이벤트 핸들러를 연결 성공 시점에 등록
                            this.socket.on('notifications', (data) => {
                                if (window.notificationBadge) {
                                    window.notificationBadge.handleAllNotifications(data);
                                }
                            });
                        });

                        // 연결 해제 이벤트 핸들러
                        this.socket.on('disconnect', () => {
                            console.log('WebSocket 연결 해제');
                            this.initialized = false;
                        });

                        // 연결 에러 이벤트 핸들러
                        this.socket.on('connect_error', (error) => {
                            console.error('WebSocket 연결 에러:', error);
                            this.initialized = false;
                        });

                        // 재연결 시도 이벤트 핸들러
                        this.socket.on('reconnect_attempt', () => {
                            console.log('WebSocket 재연결 시도 중...');
                        });

                        // 재연결 실패 이벤트 핸들러
                        this.socket.on('reconnect_failed', () => {
                            console.error('WebSocket 재연결 실패');
                            this.initialized = false;
                        });

                        // 알림 개수 업데이트 이벤트 핸들러
                        this.socket.on('notificationCount', (data) => {
                            this.updateBadge(data.count);
                        });

                        // 새 알림 수신 이벤트 핸들러
                        this.socket.on('newNotification', (notification) => {
                            this.incrementBadge();
                            this.showNotificationToast(notification);
                            // 새 알림 수신 시 전체 목록을 다시 요청
                            this.socket.emit('findAllNotifications');
                        });
                    } else {
                        // Socket.IO가 아직 로드되지 않았다면 100ms 후에 다시 시도
                        setTimeout(initSocket, 100);
                    }
                };

                // Socket.IO 초기화 시작
                initSocket();
            } catch (error) {
                console.error('WebSocket 초기화 실패:', error);
            }
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

    // 토스트 알림 표시
    showNotificationToast(notification) {
        const toast = document.createElement('div');
        toast.className = 'notification-toast';
        toast.innerHTML = `
            <div class="notification-toast-content">
                <div class="notification-toast-title">${notification.title || '새 알림'}</div>
                <div class="notification-toast-message">${notification.message}</div>
            </div>
        `;

        document.body.appendChild(toast);

        // 3초 후 토스트 제거
        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, 3000);
    }

    destroy() {
        if (this.socket) {
            this.socket.disconnect();
            this.socket = null;
            this.initialized = false;
        }
    }

    // Socket이 연결되었는지 확인
    isConnected() {
        return this.socket && this.socket.connected;
    }

    // 안전한 emit 함수
    safeEmit(event, data) {
        if (this.isConnected()) {
            this.socket.emit(event, data);
            return true;
        }
        console.warn('Socket이 연결되지 않았습니다. 이벤트를 보낼 수 없습니다:', event);
        return false;
    }

    // 외부에서 알림 데이터를 처리할 수 있도록 메서드 추가
    handleAllNotifications(data) {
        if (typeof window.setAllNotifications === 'function') {
            window.setAllNotifications(data.notifications || []);
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

    // NotificationBadge 클래스에서 호출될 수 있도록 전역 함수로 정의
    window.setAllNotifications = (notifications) => {
        allNotifications = notifications;
        renderNotifications(currentCategory); // 데이터가 업데이트되면 항상 다시 렌더링
    };

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

    // 알림 목록 로드 및 렌더링
    async function loadAndRenderNotifications() {
        // Socket.IO가 연결될 때까지 대기
        if (!window.notificationBadge || !window.notificationBadge.isConnected()) {
            console.log('Socket 연결 대기 중... 재시도합니다.');
            setTimeout(loadAndRenderNotifications, 500);
            return;
        }

        // 모달의 HTML 구조를 예전 방식으로 복원
        if (!notificationModal.querySelector('.notification-modal-header')) {
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
                </div>
                <div class="notification-modal-list"><p style="padding: 15px; text-align: center;">로딩 중...</p></div>
            `;
        }

        // 알림 데이터 요청
        window.notificationBadge.safeEmit('findAllNotifications');
    }

    // 이벤트 위임
    notificationModal.addEventListener('click', async function(event) {
        const item = event.target.closest('.notification-item');
        const tab = event.target.closest('.notification-tab');
        const settingsBtn = event.target.closest('.notification-settings-btn');

        if (tab) {
            // 탭 클릭 시 해당 카테고리의 알림을 다시 렌더링
            document.querySelectorAll('.notification-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            renderNotifications(tab.dataset.category);
            return;
        }

        if (settingsBtn) {
            // WebSocket을 통해 모든 알림 읽음 처리 요청
            if (window.notificationBadge && window.notificationBadge.isConnected()) {
                window.notificationBadge.socket.emit('markAllAsRead');
                console.log('모든 알림 읽음 처리 요청');
            }
            return;
        }

        if (item) {
            const notificationId = item.dataset.id;
            const link = item.dataset.link;

            // 읽지 않은 알림만 처리
            if (!item.classList.contains('read')) {
                // WebSocket을 통해 알림 읽음 처리 요청
                if (window.notificationBadge && window.notificationBadge.isConnected()) {
                    window.notificationBadge.socket.emit('markAsRead', { notificationId: parseInt(notificationId, 10) });
                    console.log(`알림 ${notificationId} 읽음 처리 요청`);
                }
            }

            // 링크가 있으면 해당 페이지로 이동
            if (link && link !== 'null') {
                window.location.href = link;
            }
        }
    });
});

// 토스트 알림 스타일 추가
const style = document.createElement('style');
style.textContent = `
    .notification-toast {
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        padding: 16px;
        z-index: 9999;
        max-width: 300px;
        animation: slide-in 0.3s ease-out;
    }

    .notification-toast.fade-out {
        animation: fade-out 0.3s ease-out;
    }

    .notification-toast-title {
        font-weight: bold;
        margin-bottom: 4px;
    }

    .notification-toast-message {
        color: #666;
    }

    @keyframes slide-in {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes fade-out {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;

document.head.appendChild(style);

// 현재 사용자 ID를 가져오는 함수
function getCurrentUserId() {
    const bellContainer = document.querySelector('.bell-container');
    return bellContainer ? bellContainer.getAttribute('data-user-id') : null;
} 