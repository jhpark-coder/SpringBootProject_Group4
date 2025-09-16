# 🎨 Nexus - Creative Auction Platform

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen?logo=springboot)
![React](https://img.shields.io/badge/React-18.3.1-61DAFB?logo=react)
![NestJS](https://img.shields.io/badge/NestJS-11.0.1-E0234E?logo=nestjs)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)

**실시간 입찰과 창작물 거래를 위한 종합 경매 플랫폼**

[🚀 시작하기](#-빠른-시작) • [📚 문서](#-프로젝트-구조) • [💡 기능](#-주요-기능)

</div>

---

## 📌 프로젝트 소개

**Nexus**는 창작자와 구매자를 연결하는 실시간 경매 플랫폼입니다. 디지털 창작물, 예술 작품, 프로젝트를 실시간으로 거래할 수 있으며, WebSocket 기반의 실시간 입찰 시스템과 리치 에디터를 통한 상세한 상품 소개가 가능합니다.


## 🏗️ 시스템 아키텍처

### Frontend
- **Thymeleaf**: 서버 사이드 렌더링 (SSR)
- **React**: 채팅 관리자 및 에디터 컴포넌트

### Backend
- **Spring Boot** (Port 8080): 메인 API 서버
- **NestJS** (Port 3000): WebSocket 실시간 통신

### Data
- **MariaDB**: 메인 데이터베이스
- **Redis**: 캐싱 및 세션 관리

## 🚀 빠른 시작

### 필요 조건
- Java 21 이상
- Node.js 18 이상
- Docker & Docker Compose
- Maven 3.8+

### 설치 및 실행

Docker Compose를 통해 전체 서비스를 한 번에 실행하거나, 개발 환경에서 각 서비스를 개별적으로 실행할 수 있습니다.

## 🛠️ 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|-----|------|
| Spring Boot | 3.5.3 | 메인 API 서버 |
| Java | 21 | 런타임 환경 |
| Spring Security | 6.x | 인증 및 보안 |
| JPA/Hibernate | 6.x | ORM |
| QueryDSL | 5.x | 동적 쿼리 |
| NestJS | 11.0.1 | 실시간 통신 서버 |
| Socket.IO | 4.8.1 | WebSocket 통신 |

### Frontend
| 기술 | 버전 | 용도 |
|------|-----|------|
| React | 18.3.1 | SPA 프레임워크 |
| TypeScript | 5.0+ | 타입 안정성 |
| Vite | 5.x | 빌드 도구 |
| TailwindCSS | 3.4 | 스타일링 |
| TipTap | 2.x | 리치 텍스트 에디터 |
| Thymeleaf | 3.x | 서버 사이드 렌더링 |

### Infrastructure
| 기술 | 용도 |
|------|------|
| MariaDB | 메인 데이터베이스 |
| Redis | 캐싱 및 세션 관리 |
| Docker | 컨테이너화 |
| AWS RDS | 프로덕션 DB |

## 💡 주요 기능

### 경매 시스템
- 실시간 입찰 (WebSocket)
- 자동 경매 종료
- 즉시 구매 기능
- 입찰 기록 관리
- 카테고리별 분류

### 커뮤니케이션
- 실시간 채팅
- 알림 시스템
- 상품 Q&A

### 컨텐츠 관리
- TipTap 리치 텍스트 에디터
- 이미지 업로드
- 실시간 미리보기

### 결제
- 아임포트 결제 연동
- 다양한 결제 수단 지원

### 관리자
- 회원 관리
- 경매 관리
- 통계 대시보드

## 📁 프로젝트 구조

```
nexus/
├── src/
│   ├── main/
│   │   ├── java/           # Spring Boot 소스 코드
│   │   │   └── com/creatorworks/nexus/
│   │   │       ├── auction/    # 경매 모듈
│   │   │       ├── member/     # 회원 모듈
│   │   │       ├── chat/       # 채팅 모듈
│   │   │       ├── admin/      # 관리자 모듈
│   │   │       └── config/     # 설정 파일
│   │   ├── resources/
│   │   │   ├── static/         # 정적 리소스
│   │   │   ├── templates/      # Thymeleaf 템플릿
│   │   │   └── application.properties
│   │   ├── chatManager/        # React 채팅 관리자
│   │   └── editor/            # React 에디터
│   └── test/                  # 테스트 코드
├── notification-server/       # NestJS 실시간 서버
│   ├── src/
│   │   ├── chat/             # 채팅 게이트웨이
│   │   ├── notifications/    # 알림 게이트웨이
│   │   └── main.ts           # 진입점
│   └── package.json
├── presentation_slides/       # 프레젠테이션 자료
├── docker-compose.yml         # 프로덕션 Docker 구성
├── docker-compose.dev.yml     # 개발 Docker 구성
├── deploy-ec2.sh             # EC2 배포 스크립트
├── EC2_DEPLOYMENT_GUIDE.md  # EC2 배포 가이드
├── CHAT_FLOWCHART.md        # 채팅 시스템 플로우차트
├── pom.xml                   # Maven 설정
└── README.md                 # 프로젝트 문서
```

## 🌐 API 문서

### 경매 API

| 메소드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | `/auctions` | 경매 목록 조회 (페이징) |
| GET | `/auctions/{id}` | 경매 상세 조회 |
| POST | `/auctions` | 새 경매 생성 |
| POST | `/auctions/{id}/bid` | 입찰하기 |
| PUT | `/auctions/{id}` | 경매 수정 |
| DELETE | `/auctions/{id}` | 경매 삭제 |

### 채팅 API

| 메소드 | 엔드포인트 | 설명 |
|--------|----------|------|
| POST | `/api/chat/save` | 메시지 저장 |
| GET | `/api/chat/history` | 채팅 기록 조회 |

### WebSocket 이벤트

| 이벤트 | 방향 | 설명 |
|--------|------|------|
| `joinChat` | Client → Server | 채팅방 입장 |
| `sendMessage` | Client → Server | 메시지 전송 |
| `newMessage` | Server → Client | 새 메시지 수신 |
| `bidUpdate` | Server → Client | 입찰 업데이트 |

## 🔒 보안

### 인증 및 권한
- Spring Security 기반 폼 로그인
- JWT/OAuth2 리소스 서버 지원
- 역할 기반 접근 제어 (USER, ADMIN)
- BCrypt 패스워드 암호화

### 보안 적용
- Spring Security 기반 인증/인가
- BCrypt 패스워드 암호화
- JPA를 통한 SQL Injection 방지

## 🧪 테스트

Maven을 통한 단위 테스트 및 통합 테스트를 지원합니다.



## 📊 프로젝트 현황

### 구현 완료 기능
- ✅ 경매 시스템 (등록, 입찰, 낙찰)
- ✅ 실시간 입찰 업데이트 (WebSocket)
- ✅ 채팅 시스템
- ✅ 관리자 대시보드
- ✅ 결제 시스템 (아임포트 연동)
- ✅ 리치 텍스트 에디터 (TipTap)
- ✅ 실시간 알림 시스템
- ✅ 회원 가입 및 로그인
- ✅ 카테고리별 상품 분류

## 👥 개발팀

| 이름 | 역할 | 담당 영역 |
|------|------|-----------|
| **박준호** | Team Lead & Backend | Spring Boot 백엔드, DB 설계, 시스템 아키텍처, NestJS/Socket.IO 실시간 서버(채팅/알림), TipTap 에디터, Docker 및 Git 환경구축, AWS EC2 배포 |
| **박영우** | UI/UX Developer | UI/UX 디자인, 회원가입, 마이페이지, 경매 화면 구현 |
| **윤진** | Full-Stack Developer | 기초자료수집, 결제기능, 상세페이지, 소셜기능(좋아요, 팔로우) |


## 📞 문의

프로젝트 관련 문의는 이슈 트래커를 이용해 주세요.


---

<div align="center">

**Nexus Portfolio Platform © 2025**

</div>