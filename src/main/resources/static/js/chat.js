/**
 * Chat Controller - Socket.IO 기반
 * STOMP에서 Socket.IO로 마이그레이션
 */

class ChatController {
    constructor() {
        this.socket = null;
        this.username = null;
        this.isConnected = false;
        this.isAdmin = false;

        this.init();
    }

    init() {
        // Socket.IO가 로드될 때까지 대기
        if (typeof io !== 'undefined') {
            this.connectSocket();
        } else {
            console.warn('⚠️ Socket.IO 라이브러리를 찾을 수 없습니다. 1초 후 재시도...');
            setTimeout(() => this.init(), 1000);
        }
    }

    connectSocket() {
        console.log('🔗 채팅 Socket.IO 연결 시작');

        // Socket.IO 연결 설정
        this.socket = io('http://localhost:3000', {
            transports: ['websocket', 'polling']
        });

        // 연결 성공
        this.socket.on('connect', () => {
            console.log('✅ 채팅 연결 성공 - 연결 ID:', this.socket.id);
            this.isConnected = true;
            this.setupEventListeners();
        });

        // 연결 해제
        this.socket.on('disconnect', (reason) => {
            console.log('🔗 채팅 연결 해제:', reason);
            this.isConnected = false;
        });

        // 연결 오류
        this.socket.on('connect_error', (error) => {
            console.error('❌ 채팅 연결 오류:', error);
        });

        // 재연결 시도
        this.socket.on('reconnect_attempt', () => {
            console.log('🔄 채팅 재연결 시도 중...');
        });

        // 재연결 실패
        this.socket.on('reconnect_failed', () => {
            console.error('❌ 채팅 재연결 실패');
        });
    }

    setupEventListeners() {
        // 채팅 메시지 수신
        this.socket.on('chatMessage', (message) => {
            console.log('📨 채팅 메시지 수신:', message);
            this.displayMessage(message);
        });

        // 사용자 접속 알림 (관리자용)
        this.socket.on('userJoined', (message) => {
            console.log('👤 사용자 접속:', message);
            this.displaySystemMessage(message.content);
        });

        // 관리자 응답 (사용자용)
        this.socket.on('adminReply', (message) => {
            console.log('💬 관리자 응답:', message);
            this.displayMessage(message);
        });

        // 사용자 메시지 (관리자용)
        this.socket.on('userMessage', (message) => {
            console.log('💬 사용자 메시지:', message);
            this.displayMessage(message);
        });
    }

    // 사용자 접속
    joinChat(username) {
        if (!this.isConnected) {
            console.warn('⚠️ Socket이 연결되지 않았습니다.');
            return;
        }

        this.username = username;
        console.log('👤 채팅 참여:', username);

        this.socket.emit('joinChat', {
            sender: username,
            type: 'JOIN'
        });
    }

    // 메시지 전송
    sendMessage(content, recipient = null) {
        if (!this.isConnected) {
            console.warn('⚠️ Socket이 연결되지 않았습니다.');
            return;
        }

        const message = {
            content: content,
            sender: this.username,
            recipient: recipient,
            type: 'CHAT',
            timestamp: new Date().toISOString()
        };

        console.log('📤 메시지 전송:', message);
        this.socket.emit('sendMessage', message);
    }

    // 메시지 표시
    displayMessage(message) {
        const chatBody = document.querySelector('.chat-body');
        if (!chatBody) {
            console.warn('⚠️ 채팅 본문을 찾을 수 없습니다.');
            return;
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `chat-message ${this.isOwnMessage(message) ? 'sent' : 'received'}`;

        const messageContent = document.createElement('p');
        messageContent.textContent = message.content;

        const timestamp = document.createElement('small');
        timestamp.className = 'message-time';
        timestamp.textContent = this.formatTime(message.timestamp);

        messageDiv.appendChild(messageContent);
        messageDiv.appendChild(timestamp);

        chatBody.appendChild(messageDiv);
        this.scrollToBottom();
    }

    // 시스템 메시지 표시
    displaySystemMessage(content) {
        const chatBody = document.querySelector('.chat-body');
        if (!chatBody) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message system';

        const messageContent = document.createElement('p');
        messageContent.textContent = content;
        messageContent.style.fontStyle = 'italic';
        messageContent.style.color = '#666';

        messageDiv.appendChild(messageContent);
        chatBody.appendChild(messageDiv);
        this.scrollToBottom();
    }

    // 자신의 메시지인지 확인
    isOwnMessage(message) {
        return message.sender === this.username;
    }

    // 시간 포맷팅
    formatTime(timestamp) {
        if (!timestamp) return '';

        const date = new Date(timestamp);
        return date.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    // 채팅창 스크롤을 맨 아래로
    scrollToBottom() {
        const chatBody = document.querySelector('.chat-body');
        if (chatBody) {
            setTimeout(() => {
                chatBody.scrollTop = chatBody.scrollHeight;
            }, 100);
        }
    }

    // 연결 해제
    disconnect() {
        if (this.socket) {
            console.log('🔗 채팅 연결 해제 중...');
            this.socket.disconnect();
            this.socket = null;
            this.isConnected = false;
        }
    }

    // 연결 상태 확인
    isSocketConnected() {
        return this.socket && this.socket.connected;
    }

    // 이벤트 발생
    emit(event, data) {
        if (this.isSocketConnected()) {
            this.socket.emit(event, data);
        } else {
            console.warn('Socket이 연결되지 않았습니다. 이벤트를 보낼 수 없습니다:', event);
        }
    }
}

// 전역 채팅 컨트롤러 인스턴스
window.chatController = null;

// 페이지 로드 시 채팅 초기화
document.addEventListener('DOMContentLoaded', function () {
    // 채팅 컨트롤러 초기화
    window.chatController = new ChatController();

    // 채팅 입력 이벤트 설정
    setupChatInputEvents();
});

// 채팅 입력 이벤트 설정
function setupChatInputEvents() {
    const chatInput = document.getElementById('chat-input');
    const sendButton = document.getElementById('send-chat-btn');
    const closeButton = document.getElementById('close-chat-btn');

    if (chatInput && sendButton) {
        // 전송 버튼 클릭
        sendButton.addEventListener('click', () => {
            sendChatMessage();
        });

        // Enter 키 입력
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                sendChatMessage();
            }
        });
    }

    if (closeButton) {
        // 채팅창 닫기
        closeButton.addEventListener('click', () => {
            closeChat();
        });
    }
}

// 채팅 메시지 전송
function sendChatMessage() {
    const chatInput = document.getElementById('chat-input');
    if (!chatInput || !window.chatController) return;

    const message = chatInput.value.trim();
    if (!message) return;

    // 사용자 이름이 설정되지 않았다면 임시로 설정
    if (!window.chatController.username) {
        const currentUser = getCurrentUsername();
        window.chatController.joinChat(currentUser);
    }

    window.chatController.sendMessage(message);
    chatInput.value = '';
}

// 현재 사용자 이름 가져오기
function getCurrentUsername() {
    // Thymeleaf에서 전달된 사용자 정보가 있다면 사용
    const userElement = document.querySelector('[data-username]');
    if (userElement) {
        return userElement.getAttribute('data-username');
    }

    // 세션에서 사용자 정보 가져오기
    const sessionUser = document.querySelector('[data-session-user]');
    if (sessionUser) {
        return sessionUser.getAttribute('data-session-user');
    }

    // 기본값
    return '사용자_' + Math.random().toString(36).substr(2, 9);
}

// 채팅창 닫기
function closeChat() {
    const chatContainer = document.getElementById('chat-container');
    if (chatContainer) {
        chatContainer.style.display = 'none';
    }

    // 채팅 연결 해제
    if (window.chatController) {
        window.chatController.disconnect();
    }
}

// 채팅창 열기
function openChat() {
    const chatContainer = document.getElementById('chat-container');
    if (chatContainer) {
        chatContainer.style.display = 'flex';

        // 사용자 접속
        if (window.chatController && !window.chatController.username) {
            const currentUser = getCurrentUsername();
            window.chatController.joinChat(currentUser);
        }
    }
}

// 전역 함수로 노출
window.openChat = openChat;
window.closeChat = closeChat;
window.sendChatMessage = sendChatMessage; 