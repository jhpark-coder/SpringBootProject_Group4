# Docker 빌드 명령어 가이드

## 🐳 **기본 도커 빌드 명령어**

### 1. **전체 프로젝트 빌드**
```bash
# 프로젝트 루트에서 실행
docker-compose up --build
```

### 2. **개별 서비스 빌드**
```bash
# 메인 애플리케이션만 빌드
docker build -t nexus-app .

# 알림 서버만 빌드
docker build -f notification-server/Dockerfile -t nexus-notification ./notification-server

# 채팅 매니저만 빌드
docker build -f src/main/chatManager/Dockerfile -t nexus-chat-manager ./src/main/chatManager
```

### 3. **개발 환경 빌드**
```bash
# 개발용 도커 컴포즈
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

### 4. **프로덕션 환경 빌드**
```bash
# 프로덕션용 도커 컴포즈
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build
```

## 🔧 **빌드 최적화 옵션**

### 1. **캐시 없이 완전 재빌드**
```bash
docker-compose build --no-cache
```

### 2. **특정 서비스만 재빌드**
```bash
docker-compose build --no-cache app
docker-compose build --no-cache notification-server
```

### 3. **멀티 스테이지 빌드 (최적화)**
```bash
# 최적화된 이미지 빌드
docker build --target production -t nexus-app-optimized .
```

## 🚀 **실행 명령어**

### 1. **백그라운드 실행**
```bash
docker-compose up -d
```

### 2. **로그 확인**
```bash
# 전체 로그
docker-compose logs

# 특정 서비스 로그
docker-compose logs app
docker-compose logs notification-server

# 실시간 로그
docker-compose logs -f
```

### 3. **서비스 상태 확인**
```bash
docker-compose ps
```

## 🛠️ **개발 도구**

### 1. **컨테이너 접속**
```bash
# 메인 앱 컨테이너 접속
docker-compose exec app bash

# 알림 서버 컨테이너 접속
docker-compose exec notification-server sh
```

### 2. **데이터베이스 접속**
```bash
# H2 데이터베이스 접속
docker-compose exec app java -cp h2-*.jar org.h2.tools.Shell -url jdbc:h2:./data/testdb
```

### 3. **볼륨 확인**
```bash
docker volume ls
docker volume inspect nexus_upload-data
```

## 🔍 **문제 해결**

### 1. **포트 충돌 해결**
```bash
# 사용 중인 포트 확인
netstat -tulpn | grep :8080

# 컨테이너 중지
docker-compose down

# 포트 변경 후 재시작
docker-compose up -d
```

### 2. **이미지 정리**
```bash
# 사용하지 않는 이미지 삭제
docker image prune -a

# 전체 정리
docker system prune -a
```

### 3. **볼륨 정리**
```bash
# 사용하지 않는 볼륨 삭제
docker volume prune
```

## 📊 **성능 모니터링**

### 1. **리소스 사용량 확인**
```bash
docker stats
```

### 2. **컨테이너 상세 정보**
```bash
docker inspect nexus-app-1
```

## 🎯 **JavaScript 모듈화 테스트**

### 1. **브라우저에서 확인할 점들**
- [ ] 모든 페이지가 정상 로드되는지
- [ ] 콘솔 에러가 없는지
- [ ] 모듈 로딩 순서가 올바른지
- [ ] 기능들이 정상 작동하는지

### 2. **성능 체크**
- [ ] 페이지 로드 시간
- [ ] 모듈 로드 시간
- [ ] 메모리 사용량
- [ ] 네트워크 요청 수

### 3. **기능 테스트**
- [ ] 회원가입/로그인
- [ ] 챗봇 버튼 표시
- [ ] 좋아요/팔로우 기능
- [ ] 알림 시스템
- [ ] 그리드 애니메이션
- [ ] 무한 스크롤

## 🔄 **배포 워크플로우**

### 1. **개발 → 테스트**
```bash
# 개발 환경 빌드
docker-compose -f docker-compose.dev.yml up --build

# 테스트 실행
docker-compose exec app mvn test
```

### 2. **테스트 → 스테이징**
```bash
# 스테이징 환경 빌드
docker-compose -f docker-compose.staging.yml up --build
```

### 3. **스테이징 → 프로덕션**
```bash
# 프로덕션 환경 빌드
docker-compose -f docker-compose.prod.yml up --build
```

## 📝 **주의사항**

1. **환경 변수**: `.env` 파일이 올바르게 설정되어 있는지 확인
2. **포트 충돌**: 8080, 3000, 8081 포트가 사용 가능한지 확인
3. **볼륨 권한**: 업로드 폴더에 쓰기 권한이 있는지 확인
4. **메모리**: 도커 데몬에 충분한 메모리가 할당되어 있는지 확인

## 🎉 **성공적인 빌드 후 확인사항**

- [ ] 모든 서비스가 정상적으로 시작됨
- [ ] 웹 애플리케이션에 접속 가능
- [ ] 데이터베이스 연결 정상
- [ ] 파일 업로드 기능 정상
- [ ] 실시간 알림 기능 정상
- [ ] JavaScript 모듈 시스템 정상 작동 