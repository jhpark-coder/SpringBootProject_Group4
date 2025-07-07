# 상담원 채팅 시스템 구조 및 주요 기능 정리

---

## 1. 프론트엔드 (관리자/사용자)

### 📁 React 관리자 대시보드
- **폴더:** `src/main/chatManager/src/`
- **주요 파일 및 기능:**
  - `ChatDashboard.jsx`
    - 소켓 연결/해제 (`useEffect`)
    - 사용자 선택/채팅방 진입 (`selectUser`)
    - 실시간 메시지 수신 (`socket.on('userMessage')`, `socket.on('adminReply')`)
    - 과거 내역 요청/수신 (`socket.emit('getHistory')`, `socket.on('chatHistory')`)
  - `ChatRoom.jsx`
    - 채팅방 UI 렌더링
    - 메시지 입력/전송
    - 메시지 리스트 렌더링

### 📁 사용자 채팅창 (Vanilla JS)
- **폴더:** `src/main/resources/static/js/`
- **주요 파일 및 기능:**
  - `chat.js`
    - 소켓 연결/해제 (`this.socket = io(...)`)
    - 메시지 전송 (`this.socket.emit('sendMessage', ...)`)
    - 실시간 메시지 수신 (`this.socket.on('adminReply', ...)`)
    - 채팅창 UI 동적 갱신 (`addMessageToChatbox`)

---

## 2. 실시간 통신 서버 (NestJS)

### 📁 NestJS 채팅 서버
- **폴더:** `notification-server/src/chat/`
- **주요 파일 및 기능:**
  - `chat.gateway.ts`
    - WebSocket 연결 관리 (`@WebSocketGateway`)
    - 메시지 수신/중계 (`@SubscribeMessage('sendMessage')`)
    - 관리자/사용자 그룹 관리
    - 실시간 이벤트 전송 (`server.to(...).emit(...)`)
    - 과거 내역 요청 처리 (`@SubscribeMessage('getHistory')`)
  - `chat.service.ts`
    - 메시지 저장 요청 (`saveMessage` → fetch로 Spring Boot 호출)
    - 채팅 내역 조회 요청 (`getChatHistory` → fetch로 Spring Boot 호출)
    - 온라인 사용자 관리

---

## 3. 백엔드 서버 (Spring Boot)

### 📁 채팅 API
- **폴더:** `src/main/java/com/creatorworks/nexus/chat/`
- **주요 파일 및 기능:**
  - `controller/ChatController.java`
    - 메시지 저장 API (`@PostMapping` → `saveMessage`)
    - 채팅 내역 조회 API (`@GetMapping` → `getChatHistory`, `getChatHistoryByPattern`, `getAllMessages`)
  - `service/ChatMessageService.java`
    - 메시지 저장 비즈니스 로직 (`saveMessage`)
    - 내역 조회/삭제 (`getChatHistory`, `getAllMessages`, `deleteUserMessages` 등)
  - `repository/ChatMessageRepository.java`
    - JPA 쿼리 메소드 (메시지 저장/조회)
  - `entity/ChatMessage.java`, `ChatMessageType.java`
    - 채팅 메시지 엔티티/타입 정의

---

## 4. 데이터베이스 (MySQL)
- **테이블:** `CHAT_MESSAGE`
  - 컬럼: `ID`, `SENDER`, `RECIPIENT`, `CONTENT`, `TYPE`, `TIMESTAMP`

---

## 참고: 전체 흐름 요약

1. **메시지 전송:**
   - (관리자/사용자) → [WebSocket] → NestJS → [HTTP] → Spring Boot → [JPA] → DB
2. **과거 내역 조회:**
   - (관리자/사용자) → [WebSocket] → NestJS → [HTTP] → Spring Boot → [JPA] → DB → [WebSocket] → (관리자/사용자)
3. **실시간 메시지 수신:**
   - NestJS가 WebSocket으로 상대방에게 즉시 전달

---

> 이 파일을 보면서 각 계층별 역할과 주요 코드를 빠르게 익힐 수 있습니다! 