# 채팅 시스템 마이그레이션 가이드

## 📋 개요

기존 STOMP(Spring Boot) 기반 채팅 시스템을 NestJS(Socket.IO)로 성공적으로 마이그레이션했습니다.

## 🚀 주요 변경사항

### 1. 기술 스택 변경
- **이전**: Spring Boot + STOMP + SockJS
- **현재**: NestJS + Socket.IO

### 2. 아키텍처 개선
- **단일 서버 통합**: 채팅과 알림을 하나의 NestJS 서버로 통합
- **타입 안전성**: TypeScript로 더 나은 개발 경험
- **확장성**: Socket.IO의 클러스터링 지원

## 🔧 설치 및 실행

### 1. NestJS 서버 실행
```bash
cd notification-server
npm install
npm run start:dev
```

### 2. Spring Boot 서버 실행
```bash
mvn spring-boot:run
```

## 📱 사용 방법

### 사용자 채팅
1. 메인 페이지 접속
2. 우측 하단의 파란색 채팅 버튼 클릭
3. 메시지 입력 후 전송

### 관리자 대시보드
1. `http://localhost:8080/admin/chat` 접속
2. 실시간 사용자 목록 확인
3. 사용자 선택 후 1:1 채팅 진행

## 🔌 API 엔드포인트

### Socket.IO 이벤트

#### 클라이언트 → 서버
- `joinChat` - 사용자 채팅 참가
- `joinAsAdmin` - 관리자 채팅 참가
- `sendMessage` - 메시지 전송

#### 서버 → 클라이언트
- `chatMessage` - 일반 채팅 메시지
- `userMessage` - 사용자 메시지 (관리자용)
- `adminReply` - 관리자 응답 (사용자용)
- `userJoined` - 사용자 접속 알림
- `userDisconnected` - 사용자 연결 해제 알림
- `onlineUsers` - 온라인 사용자 목록

## 📁 파일 구조

```
notification-server/
├── src/
│   ├── chat/
│   │   ├── chat.gateway.ts      # Socket.IO 게이트웨이
│   │   ├── chat.service.ts      # 채팅 비즈니스 로직
│   │   ├── chat.module.ts       # NestJS 모듈
│   │   ├── dto/
│   │   │   └── chat-message.dto.ts
│   │   └── *.spec.ts           # 테스트 파일들
│   └── app.module.ts

src/main/resources/
├── static/js/
│   ├── chat.js                 # Socket.IO 채팅 컨트롤러
│   └── chat-buttons.js         # 채팅 버튼 이벤트
├── templates/
│   ├── fragment/
│   │   ├── chat.html           # 채팅 UI
│   │   └── layout.html         # 레이아웃 (Socket.IO 라이브러리 포함)
│   └── admin/
│       └── chat-dashboard.html # 관리자 대시보드
```

## 🧪 테스트

### NestJS 테스트 실행
```bash
cd notification-server
npm test
```

### Spring Boot 테스트 실행
```bash
mvn test
```

## 🔍 디버깅

### 브라우저 개발자 도구
1. F12 키로 개발자 도구 열기
2. Console 탭에서 Socket.IO 연결 상태 확인
3. Network 탭에서 WebSocket 연결 확인

### 로그 확인
- **NestJS**: `notification-server` 콘솔에서 로그 확인
- **Spring Boot**: 애플리케이션 로그에서 오류 확인

## ⚠️ 주의사항

### 1. 서버 실행 순서
1. NestJS 서버 먼저 실행 (포트 3000)
2. Spring Boot 서버 실행 (포트 8080)

### 2. 브라우저 호환성
- Socket.IO는 모든 모던 브라우저 지원
- IE11 이하 버전은 지원하지 않음

### 3. 네트워크 설정
- 방화벽에서 포트 3000, 8080 허용 필요
- 프록시 환경에서는 WebSocket 설정 확인

## 🚨 문제 해결

### Socket.IO 연결 실패
1. NestJS 서버가 실행 중인지 확인
2. 브라우저 콘솔에서 오류 메시지 확인
3. 네트워크 연결 상태 확인

### 채팅 메시지 전송 안됨
1. Socket.IO 연결 상태 확인
2. 브라우저 개발자 도구에서 이벤트 확인
3. 서버 로그에서 오류 확인

### 관리자 대시보드 접속 안됨
1. Spring Boot 서버 실행 상태 확인
2. URL 경로 확인 (`/admin/chat`)
3. 브라우저 캐시 삭제 후 재시도

## 📈 성능 최적화

### 1. 메모리 사용량
- 온라인 사용자 목록 정기 정리
- 오래된 메시지 자동 삭제

### 2. 네트워크 최적화
- Socket.IO 압축 활성화
- 불필요한 이벤트 최소화

### 3. 확장성
- Redis 어댑터로 다중 서버 지원
- 로드 밸런서 설정

## 🔮 향후 계획

### 단기 계획
- [ ] 파일 첨부 기능
- [ ] 이모지 지원
- [ ] 읽음 확인 기능

### 장기 계획
- [ ] JWT 인증 통합
- [ ] 채팅방 기능
- [ ] 메시지 암호화
- [ ] 채팅 통계 분석

---

**마이그레이션 완료일**: 2025-07-06  
**버전**: 1.0.0  
**담당자**: AI Assistant 