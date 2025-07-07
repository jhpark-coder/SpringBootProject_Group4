# STOMP → NestJS 마이그레이션 체크리스트

## 📋 마이그레이션 전 준비사항

### ✅ 현재 상태 확인
- [ ] 현재 STOMP 채팅 기능이 정상 작동하는지 확인
- [ ] 기존 테스트 코드 실행하여 모든 테스트 통과 확인
- [ ] NestJS 알림 서버가 정상 실행 중인지 확인
- [ ] 프론트엔드 채팅 UI가 정상 표시되는지 확인

### ✅ 백업 및 버전 관리
- [ ] 현재 코드를 Git 브랜치로 백업
- [ ] 데이터베이스 스키마 백업 (필요시)
- [ ] 환경 설정 파일 백업

## 🔄 1단계: NestJS 채팅 모듈 구현

### ✅ NestJS 채팅 Gateway 구현
- [ ] `notification-server/src/chat/chat.gateway.ts` 생성
- [ ] WebSocket 연결 처리 구현
- [ ] 메시지 수신/전송 로직 구현
- [ ] 사용자 방 관리 로직 구현

### ✅ NestJS 채팅 Service 구현
- [ ] `notification-server/src/chat/chat.service.ts` 생성
- [ ] 메시지 저장/조회 로직 구현
- [ ] 채팅 히스토리 관리 로직 구현

### ✅ DTO 및 모듈 설정
- [ ] `notification-server/src/chat/dto/chat-message.dto.ts` 생성
- [ ] `notification-server/src/chat/chat.module.ts` 생성
- [ ] `notification-server/src/app.module.ts`에 채팅 모듈 추가

### ✅ NestJS 테스트 코드 작성
- [ ] `notification-server/src/chat/chat.gateway.spec.ts` 작성
- [ ] `notification-server/src/chat/chat.service.spec.ts` 작성
- [ ] 모든 NestJS 테스트 통과 확인

## 🔄 2단계: Spring Boot STOMP 코드 제거

### ✅ WebSocket 설정 제거
- [ ] `src/main/java/com/creatorworks/nexus/config/WebSocketConfig.java` 삭제
- [ ] `pom.xml`에서 STOMP 의존성 제거
- [ ] Spring Boot 애플리케이션에서 WebSocket 관련 설정 제거

### ✅ 채팅 컨트롤러 제거
- [ ] `src/main/java/com/creatorworks/nexus/chat/ChatController.java` 삭제
- [ ] `src/main/java/com/creatorworks/nexus/chat/ChatMessage.java` 삭제
- [ ] 채팅 관련 패키지 정리

### ✅ 알림 서비스 수정
- [ ] `NotificationService.java`에서 NestJS 채팅 서버로 알림 전송하도록 수정
- [ ] HTTP API 엔드포인트 추가 (필요시)

## 🔄 3단계: 프론트엔드 JavaScript 수정

### ✅ Socket.IO 클라이언트 추가
- [ ] `layout.html`에 Socket.IO 클라이언트 라이브러리 추가
- [ ] SockJS, STOMP 라이브러리 제거
- [ ] 기존 STOMP 연결 코드를 Socket.IO로 변경

### ✅ 메시지 전송/수신 로직 수정
- [ ] 메시지 전송 로직을 Socket.IO 이벤트로 변경
- [ ] 메시지 수신 로직을 Socket.IO 이벤트로 변경
- [ ] 연결 관리 로직 수정

### ✅ 에러 처리 및 재연결 로직
- [ ] 연결 실패 시 재연결 로직 구현
- [ ] 에러 메시지 표시 로직 구현
- [ ] 네트워크 상태 감지 로직 구현

## 🔄 4단계: 통합 및 테스트

### ✅ 단위 테스트 실행
- [ ] Spring Boot 단위 테스트 실행
- [ ] NestJS 단위 테스트 실행
- [ ] 모든 테스트 통과 확인

### ✅ 통합 테스트 실행
- [ ] WebSocket 연결 테스트
- [ ] 메시지 전송/수신 테스트
- [ ] 사용자 연결/해제 테스트
- [ ] 관리자 기능 테스트

### ✅ 브라우저 테스트
- [ ] Chrome에서 채팅 기능 테스트
- [ ] Firefox에서 채팅 기능 테스트
- [ ] Safari에서 채팅 기능 테스트 (필요시)
- [ ] 모바일 브라우저 테스트 (필요시)

## 🔄 5단계: 성능 및 안정성 검증

### ✅ 성능 테스트
- [ ] 동시 사용자 연결 테스트
- [ ] 메시지 전송 성능 테스트
- [ ] 메모리 사용량 모니터링
- [ ] CPU 사용량 모니터링

### ✅ 안정성 테스트
- [ ] 장시간 연결 유지 테스트
- [ ] 네트워크 끊김 복구 테스트
- [ ] 서버 재시작 후 연결 복구 테스트
- [ ] 메시지 손실 방지 테스트

## 🔄 6단계: 배포 및 모니터링

### ✅ 배포 준비
- [ ] 프로덕션 환경 설정 확인
- [ ] 환경 변수 설정 확인
- [ ] 로그 설정 확인
- [ ] 모니터링 도구 설정

### ✅ 배포 실행
- [ ] Spring Boot 애플리케이션 배포
- [ ] NestJS 서버 배포
- [ ] 프론트엔드 배포
- [ ] DNS/로드밸런서 설정 (필요시)

### ✅ 배포 후 검증
- [ ] 모든 기능 정상 작동 확인
- [ ] 로그 모니터링
- [ ] 에러 알림 설정
- [ ] 사용자 피드백 수집

## 📊 마이그레이션 완료 체크리스트

### ✅ 기능 검증
- [ ] 사용자 채팅 기능 정상 작동
- [ ] 관리자 채팅 기능 정상 작동
- [ ] 실시간 알림 기능 정상 작동
- [ ] 기존 기능에 영향 없음

### ✅ 성능 검증
- [ ] 응답 시간 기존 대비 유지 또는 개선
- [ ] 메모리 사용량 적정 수준 유지
- [ ] CPU 사용량 적정 수준 유지
- [ ] 네트워크 대역폭 사용량 적정

### ✅ 안정성 검증
- [ ] 24시간 연속 운영 테스트
- [ ] 장애 복구 테스트
- [ ] 백업 및 복원 테스트
- [ ] 보안 취약점 검사

## 🚨 롤백 계획

### ✅ 롤백 준비
- [ ] 이전 버전 코드 보관
- [ ] 데이터베이스 롤백 스크립트 준비
- [ ] 롤백 절차 문서화
- [ ] 롤백 담당자 지정

### ✅ 롤백 트리거 조건
- [ ] 주요 기능 장애 발생 시
- [ ] 성능 저하 발생 시
- [ ] 보안 취약점 발견 시
- [ ] 사용자 불만 지속 시

---

## 📝 마이그레이션 일정

- **1단계**: NestJS 채팅 모듈 구현 (2-3일)
- **2단계**: Spring Boot STOMP 코드 제거 (1일)
- **3단계**: 프론트엔드 JavaScript 수정 (2-3일)
- **4단계**: 통합 및 테스트 (2-3일)
- **5단계**: 성능 및 안정성 검증 (1-2일)
- **6단계**: 배포 및 모니터링 (1일)

**총 예상 소요 시간**: 9-13일

---

## 📞 지원 및 문의

마이그레이션 과정에서 문제가 발생하면:
1. 로그 확인
2. 테스트 코드 실행
3. 문서 참조
4. 필요시 외부 지원 요청 