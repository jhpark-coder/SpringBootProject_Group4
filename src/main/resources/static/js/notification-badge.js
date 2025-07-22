console.log('notification-badge.js loaded');
// 알림 배지 관리
class NotificationBadge {
    constructor(badge) {
        this.badge = badge;
        this.socket = null;
        this.initialized = false;
        this.processedNotifications = new Set(); // 처리된 알림 ID 저장
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 3;
        // DOMContentLoaded 이후에 초기화
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.init());
        } else {
            this.init();
        }
    }

    init() {
        if (this.badge && !this.initialized) {
            console.log('🔔 NotificationBadge 초기화 시작');
            console.log('🔔 배지 요소:', this.badge);

            try {
                const bellContainer = document.querySelector('.bell-container');
                const userId = bellContainer ? parseInt(bellContainer.dataset.userId) : null;

                console.log('🔔 벨 컨테이너:', bellContainer);
                console.log('🔔 사용자 ID:', userId);

                if (!userId) {
                    console.warn('⚠️ 사용자 ID를 찾을 수 없습니다.');
                    return;
                }

                // 기존 연결이 있다면 해제
                if (this.socket) {
                    console.log('🔔 기존 WebSocket 연결 해제');
                    this.socket.disconnect();
                    this.socket = null;
                }

                // Socket.IO가 로드될 때까지 대기
                const initSocket = () => {
                    if (typeof io !== 'undefined') {
                        console.log('🔔 Socket.IO 초기화 시작');
                        
                        // 현재 호스트의 포트 3000으로 연결 (배포 환경 대응)
                        const socketUrl = window.location.hostname === 'localhost' 
                            ? 'http://localhost:3000' 
                            : `http://${window.location.hostname}:3000`;
                        
                        // Socket.IO 연결 설정
                        this.socket = io(socketUrl, {
                            withCredentials: true,
                            transports: ['websocket', 'polling'], // fallback 추가
                            autoConnect: true,
                            reconnection: true, // 자동 재연결 활성화
                            reconnectionAttempts: 5, // 최대 5회 재시도
                            reconnectionDelay: 2000, // 재연결 간격 2초
                            timeout: 10000, // 연결 타임아웃 10초
                            auth: {
                                userId: userId,
                                roles: window.currentUser ? window.currentUser.roles : []
                            }
                        });

                        // 연결 이벤트 핸들러
                        this.socket.on('connect', () => {
                            console.log('✅ WebSocket 연결 성공 - 연결 ID:', this.socket.id);
                            this.initialized = true;
                            this.reconnectAttempts = 0;
                            // 초기 알림 개수를 Spring Boot API에서 직접 가져오기
                            this.loadNotificationCount();

                            // WebSocket은 실시간 알림만 처리
                            this.socket.on('notifications', (data) => {
                                if (window.notificationBadge) {
                                    window.notificationBadge.handleAllNotifications(data);
                                }
                            });
                        });

                        // 연결 해제 이벤트 핸들러
                        this.socket.on('disconnect', (reason) => {
                            console.log('🔔 WebSocket 연결 해제:', reason);
                            this.initialized = false;

                            // 자동 재연결 시도 (최대 3회)
                            if (this.reconnectAttempts < this.maxReconnectAttempts && reason !== 'io client disconnect') {
                                this.reconnectAttempts++;
                                console.log(`🔔 재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
                                setTimeout(() => {
                                    if (!this.socket.connected) {
                                        this.socket.connect();
                                    }
                                }, 2000 * this.reconnectAttempts); // 재연결 간격 증가
                            }
                        });

                        // 연결 오류 이벤트 핸들러
                        this.socket.on('connect_error', (error) => {
                            console.error('❌ WebSocket 연결 오류:', error);
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
                            console.log('🔔 새 알림 수신:', notification);

                            // 알림 고유 식별자 생성 (ID + 시간 + 내용으로 중복 방지)
                            const notificationKey = `${notification.id || 'unknown'}_${notification.message || ''}_${notification.createdAt || Date.now()}`;

                            // 이미 처리된 알림인지 확인
                            if (this.processedNotifications.has(notificationKey)) {
                                console.log('🔔 이미 처리된 알림입니다. 무시합니다:', notificationKey);
                                return;
                            }

                            // 처리된 알림으로 마크
                            this.processedNotifications.add(notificationKey);

                            // 오래된 처리 기록 정리 (메모리 절약)
                            if (this.processedNotifications.size > 100) {
                                const oldKeys = Array.from(this.processedNotifications).slice(0, 50);
                                oldKeys.forEach(key => this.processedNotifications.delete(key));
                            }

                            this.showNotificationToast(notification);

                            // 현재 사용자가 해당 알림을 볼 수 있는지 확인
                            const canViewNotification = this.canUserViewNotification(notification);
                            console.log('🔔 알림 표시 가능 여부:', canViewNotification);

                            if (canViewNotification) {
                                // 배지 숫자 즉시 증가
                                console.log('🔔 배지 숫자 증가 시도...');
                                this.incrementBadge();
                            } else {
                                console.log('🔔 현재 사용자가 볼 수 없는 알림이므로 배지 증가 안함');
                            }

                            // 알림 모달이 열려있다면 목록도 다시 로드
                            const modal = document.getElementById('notificationModal');
                            if (modal && modal.style.display === 'flex') {
                                if (window.notificationList && typeof window.notificationList.loadNotifications === 'function') {
                                    window.notificationList.loadNotifications();
                                } else {
                                    console.error('❌ notificationList.loadNotifications 함수를 찾을 수 없습니다!');
                                }
                            }
                        });

                        // 초기 연결 시도
                        this.socket.connect();
                    } else {
                        console.warn('⚠️ Socket.IO 라이브러리를 찾을 수 없습니다. 1초 후 재시도...');
                        setTimeout(initSocket, 1000);
                    }
                };

                // Socket.IO 초기화 시작
                initSocket();
            } catch (error) {
                console.error('❌ NotificationBadge 초기화 중 오류:', error);
            }
        }
    }

    updateBadge(count) {
        console.log('🔔 updateBadge 호출 - count:', count, 'badge 요소:', this.badge);
        if (!this.badge) {
            console.error('❌ 배지 요소를 찾을 수 없습니다!');
            return;
        }

        if (count > 0) {
            this.badge.textContent = count > 99 ? '99+' : count.toString();
            this.badge.style.display = 'flex';
            console.log('✅ 배지 표시 - 개수:', this.badge.textContent);
        } else {
            this.badge.style.display = 'none';
            console.log('✅ 배지 숨김');
        }
    }

    // Spring Boot API에서 알림 개수 로드
    async loadNotificationCount() {
        console.log('🔔 알림 개수 로드 시작...');
        try {
            const response = await fetch('/api/notifications/count', {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log('✅ 알림 개수 로드 성공:', data);
            this.updateBadge(data.count);
        } catch (error) {
            console.error('❌ 알림 개수 로드 실패:', error);
        }
    }

    // 배지 숫자 증가
    incrementBadge() {
        console.log('🔔 배지 숫자 증가 시도...');
        const currentCount = parseInt(this.badge.textContent) || 0;
        this.updateBadge(currentCount + 1);
    }

    // 배지 숫자 감소
    decrementBadge() {
        console.log('🔔 배지 숫자 감소 시도...');
        const currentCount = parseInt(this.badge.textContent) || 0;
        if (currentCount > 0) {
            this.updateBadge(currentCount - 1);
        }
    }

    // 토스트 알림 표시
    showNotificationToast(notification) {
        console.log('🍞 토스트 알림 표시:', notification);

        const toastContainer = document.body;
        const existingToasts = toastContainer.querySelectorAll('.notification-toast');

        const toast = document.createElement('div');
        toast.className = 'notification-toast';

        // 동적으로 top 위치 설정
        toast.style.top = `${20 + (existingToasts.length * 85)}px`;

        toast.innerHTML = `
            <div class="notification-toast-title">🔔 새 알림</div>
            <div class="notification-toast-message">${notification.message || '알림이 도착했습니다.'}</div>
        `;

        toastContainer.appendChild(toast);
        console.log('🍞 토스트 DOM에 추가됨');

        // 등장 애니메이션
        setTimeout(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(0)';
        }, 100);

        // 5초 후 자동 사라짐
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.addEventListener('transitionend', () => {
                toast.remove();
                // 다른 토스트들 위치 재조정
                toastContainer.querySelectorAll('.notification-toast').forEach((t, index) => {
                    t.style.top = `${20 + (index * 85)}px`;
                });
            });
        }, 5000);
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

    // 현재 사용자가 해당 알림을 볼 수 있는지 확인
    canUserViewNotification(notification) {
        console.log('🔔 알림 권한 확인:', notification);

        // 현재 사용자 정보 확인 (DOM에서 가져오기)
        if (typeof window.currentUser === 'undefined' || !window.currentUser) {
            // DOM에서 사용자 정보 가져오기
            const bellContainer = document.querySelector('.bell-container');
            const currentUserId = bellContainer ? bellContainer.getAttribute('data-user-id') : null;

            if (!currentUserId) {
                console.warn('⚠️ 현재 사용자 정보를 찾을 수 없습니다.');
                return true; // 사용자 정보가 없으면 모든 알림 허용 (안전한 기본값)
            }

            // 임시로 사용자 정보 설정
            window.currentUser = {
                id: currentUserId,
                roles: [] // 역할은 서버에서 확인
            };
        }

        const userRoles = window.currentUser.roles || [];
        const currentUserId = window.currentUser.id;
        console.log('🔔 사용자 권한:', userRoles, '사용자 ID:', currentUserId);

        // 관리자 권한 확인
        const isAdmin = userRoles.includes('ROLE_ADMIN');
        const isSeller = userRoles.includes('ROLE_SELLER');
        console.log('🔔 관리자 여부:', isAdmin, '판매자 여부:', isSeller);

        // 알림 타입에 따른 접근 권한 확인
        switch (notification.category) {
            case 'ADMIN':
                // 관리자 알림은 관리자만 볼 수 있지만, 신청자용 알림은 해당 사용자도 볼 수 있음
                console.log('🔔 관리자 알림 - 권한 체크');
                const targetUserId = notification.userId || notification.targetUserId;
                const isTargetUser = targetUserId === currentUserId;
                const isAdminNotification = targetUserId === 0;

                console.log('🔔 ADMIN 알림 상세:', {
                    targetUserId: targetUserId,
                    currentUserId: currentUserId,
                    isTargetUser: isTargetUser,
                    isAdminNotification: isAdminNotification,
                    isAdmin: isAdmin
                });

                // 관리자이거나 해당 사용자에게 오는 ADMIN 알림인 경우 표시
                return isAdmin || isTargetUser;
            case 'SELLER':
                // 판매자 알림은 판매자 이상 권한이 필요
                console.log('🔔 판매자 알림 - 판매자 권한 필요');
                return isSeller || isAdmin;
            case 'AUCTION':
            case 'ORDER':
            case 'SOCIAL':
            default:
                // 일반 알림은 해당 사용자 또는 관리자가 볼 수 있음
                const generalTargetUserId = notification.userId || notification.targetUserId;
                const isGeneralTargetUser = generalTargetUserId === currentUserId;
                const isPublicNotification = generalTargetUserId === 0;
                console.log('🔔 일반 알림 - 대상 사용자 ID:', generalTargetUserId, '현재 사용자 ID:', currentUserId, '대상 일치:', isGeneralTargetUser, '공개 알림:', isPublicNotification);
                return isGeneralTargetUser || isPublicNotification || isAdmin;
        }
    }

    // 연결 해제 메서드
    disconnect() {
        if (this.socket) {
            console.log('🔔 WebSocket 연결 해제 중...');
            this.socket.disconnect();
            this.socket = null;
            this.initialized = false;
            this.processedNotifications.clear();
        }
    }
}

// 전역 변수로 설정
let notificationBadge = null;

// DOM이 완전히 로드된 후 초기화
document.addEventListener('DOMContentLoaded', function () {
    // 로그인한 사용자일 경우에만 알림 관련 모든 기능을 초기화합니다.
    if (window.currentUser && window.currentUser.id) {
        console.log('🔔 로그인 상태 확인됨 - NotificationBadge 초기화 시작');
        console.log('🔔 현재 사용자 정보:', window.currentUser);

        // 기존 인스턴스가 있다면 해제
        if (window.notificationBadge) {
            console.log('🔔 기존 NotificationBadge 인스턴스 해제');
            window.notificationBadge.disconnect();
            window.notificationBadge = null;
        }

        // 알림 배지 요소 확인
        const badge = document.getElementById('notification-badge');
        if (badge) {
            console.log('🔔 알림 배지 요소 발견:', badge);
            notificationBadge = new NotificationBadge(badge);
            window.notificationBadge = notificationBadge;
        } else {
            console.warn('⚠️ 알림 배지 요소를 찾을 수 없습니다.');
        }

        // 알림 모달 관련 초기화
        const notificationBell = document.getElementById('notification-bell-anchor');
        const bellContainer = document.querySelector('.bell-container');
        const notificationModal = document.getElementById('notificationModal');

        console.log('🔔 요소 확인 - Bell:', !!notificationBell, 'Container:', !!bellContainer, 'Modal:', !!notificationModal);

        if (notificationModal) {
            console.log('🔔 알림 모달 초기화');

            // 모달 토글 함수
            const toggleNotificationModal = function (e) {
                e.preventDefault();
                e.stopPropagation();

                if (window.notificationList) {
                    window.notificationList.toggleModal();
                } else {
                    console.error('❌ notificationList 객체를 찾을 수 없습니다!');
                }
            };

            // 여러 요소에 클릭 이벤트 등록 (더 안정적)
            if (notificationBell) {
                console.log('🔔 Bell anchor에 이벤트 리스너 등록');
                notificationBell.addEventListener('click', toggleNotificationModal);
            } else if (bellContainer) {
                // anchor가 없으면 container에라도 등록
                console.log('🔔 Bell container에 이벤트 리스너 등록');
                bellContainer.addEventListener('click', toggleNotificationModal);
            }

            // 모달 내부 클릭 시 버블링 방지
            notificationModal.addEventListener('click', function (e) {
                e.stopPropagation();
            });
        } else {
            console.error('❌ 알림 모달 요소를 찾을 수 없습니다!');
        }
    } else {
        console.log('🔔 비로그인 상태 - 알림 기능을 초기화하지 않습니다.');
        console.log('🔔 window.currentUser:', window.currentUser);
    }
});

// 전역 메서드 제공
window.incrementBadge = function () {
    if (window.notificationBadge) {
        window.notificationBadge.incrementBadge();
    } else {
        console.warn('⚠️ NotificationBadge가 초기화되지 않았습니다.');
    }
};

window.decrementBadge = function () {
    if (window.notificationBadge) {
        window.notificationBadge.decrementBadge();
    } else {
        console.warn('⚠️ NotificationBadge가 초기화되지 않았습니다.');
    }
};

// 페이지가 포커스를 받았을 때 알림 개수 갱신
window.addEventListener('focus', function () {
    if (window.notificationBadge) {
        console.log('🔔 페이지 포커스 - 알림 개수 갱신');
        window.notificationBadge.loadNotificationCount();
    }
});

// 페이지 가시성 변경 시 처리
document.addEventListener('visibilitychange', function () {
    if (window.notificationBadge) {
        if (document.hidden) {
            console.log('🔔 페이지 숨김 - 소켓 연결 해제');
            window.notificationBadge.disconnect();
        } else {
            console.log('🔔 페이지 가시성 변경 - 알림 개수 갱신');
            // 페이지가 다시 보일 때 재연결 및 알림 개수 갱신
            if (!window.notificationBadge.initialized) {
                window.notificationBadge.init();
            } else {
                window.notificationBadge.loadNotificationCount();
            }
        }
    }
});

// 페이지 언로드 시 소켓 연결 해제
window.addEventListener('beforeunload', function () {
    if (window.notificationBadge) {
        console.log('🔔 페이지 언로드 - 소켓 연결 해제');
        window.notificationBadge.disconnect();
    }
}); 