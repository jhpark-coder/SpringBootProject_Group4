# 구독 시스템 API 명세서

## 개요
구독 시스템의 REST API 명세서입니다. 작가 구독, 결제, 취소 등의 기능을 제공합니다.

## 기본 정보
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **인증**: JWT 토큰 (Authorization 헤더)

---

## 1. 구독 관련 API

### 1.1 구독 신청
**POST** `/api/subscriptions`

#### 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

#### 요청 본문
```json
{
    "productId": 1,
    "authorId": 2,
    "monthlyPrice": 9900,
    "paymentMethod": "POINT",
    "autoRenewal": true
}
```

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "data": {
        "subscriptionId": 1,
        "productId": 1,
        "productName": "아름다운 작품",
        "authorId": 2,
        "authorName": "김작가",
        "status": "ACTIVE",
        "monthlyPrice": 9900,
        "startDate": "2024-01-15T10:30:00",
        "endDate": "2024-02-15T10:30:00",
        "nextBillingDate": "2024-02-15T10:30:00",
        "autoRenewal": true,
        "cancelledAt": null,
        "cancelReason": null,
        "remainingBalance": 5000
    },
    "message": "구독이 성공적으로 시작되었습니다."
}
```

#### 응답 (실패 - 400)
```json
{
    "success": false,
    "message": "포인트가 부족합니다."
}
```

---

### 1.2 구독 취소
**POST** `/api/subscriptions/{subscriptionId}/cancel`

#### 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

#### 요청 본문
```json
{
    "reason": "가격이 비쌉니다"
}
```

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "data": {
        "subscriptionId": 1,
        "status": "CANCELLED",
        "cancelledAt": "2024-01-20T15:30:00",
        "cancelReason": "가격이 비쌉니다"
    },
    "message": "구독이 취소되었습니다."
}
```

---

### 1.3 내 구독 목록 조회
**GET** `/api/subscriptions/my?page=0&size=10`

#### 요청 헤더
```
Authorization: Bearer {JWT_TOKEN}
```

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "subscriptionId": 1,
                "productId": 1,
                "productName": "아름다운 작품",
                "authorId": 2,
                "authorName": "김작가",
                "status": "ACTIVE",
                "monthlyPrice": 9900,
                "startDate": "2024-01-15T10:30:00",
                "endDate": "2024-02-15T10:30:00",
                "nextBillingDate": "2024-02-15T10:30:00",
                "autoRenewal": true
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 10
        },
        "totalElements": 1,
        "totalPages": 1,
        "last": true,
        "first": true
    }
}
```

---

### 1.4 작가별 구독자 목록 조회
**GET** `/api/subscriptions/author/{authorId}?page=0&size=10`

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "subscriptionId": 1,
                "subscriberId": 3,
                "subscriberName": "이구독자",
                "status": "ACTIVE",
                "monthlyPrice": 9900,
                "startDate": "2024-01-15T10:30:00",
                "endDate": "2024-02-15T10:30:00"
            }
        ],
        "totalElements": 1,
        "totalPages": 1
    }
}
```

---

## 2. 포인트 관련 API

### 2.1 포인트 잔액 조회
**GET** `/api/points/balance`

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "balance": 5000
}
```

---

### 2.2 포인트 충전
**POST** `/api/points/charge`

#### 요청 본문
```json
{
    "amount": 10000,
    "paymentMethod": "CARD"
}
```

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "message": "포인트 충전이 완료되었습니다.",
    "chargedAmount": 10000,
    "newBalance": 15000
}
```

---

## 3. 결제 관련 API

### 3.1 포인트 결제
**POST** `/api/payments/point`

#### 요청 본문
```json
{
    "productId": 1,
    "amount": 9900,
    "paymentMethod": "POINT",
    "description": "상품 구매"
}
```

#### 응답 (성공 - 200)
```json
{
    "success": true,
    "data": {
        "paymentId": 1,
        "productId": 1,
        "productName": "아름다운 작품",
        "amount": 9900,
        "paymentMethod": "POINT",
        "status": "COMPLETED",
        "paymentDate": "2024-01-15T10:30:00",
        "description": "상품 구매",
        "remainingBalance": 5000
    },
    "message": "포인트 결제가 완료되었습니다."
}
```

---

## 4. 에러 코드

### 4.1 공통 에러 응답
```json
{
    "success": false,
    "message": "에러 메시지"
}
```

### 4.2 주요 에러 메시지
- `"로그인이 필요합니다."` - 인증 실패
- `"사용자를 찾을 수 없습니다."` - 사용자 정보 없음
- `"상품을 찾을 수 없습니다."` - 상품 정보 없음
- `"작가를 찾을 수 없습니다."` - 작가 정보 없음
- `"포인트가 부족합니다."` - 포인트 잔액 부족
- `"이미 구독 중인 작가입니다."` - 중복 구독
- `"구독을 찾을 수 없습니다."` - 구독 정보 없음
- `"구독을 취소할 권한이 없습니다."` - 권한 없음

---

## 5. 크론잡 스케줄러

### 5.1 구독 갱신 처리
- **스케줄**: 매일 자정 (`0 0 0 * * *`)
- **기능**: 
  - 만료 예정 구독 조회
  - 포인트 잔액 확인
  - 자동 갱신 또는 만료 처리
  - 결제 이력 저장

### 5.2 만료된 구독 처리
- **스케줄**: 매일 자정 (`0 0 0 * * *`)
- **기능**:
  - 만료된 구독 상태 변경
  - 로그 기록

---

## 6. 프론트엔드 연동

### 6.1 구독 모달 호출
```javascript
// 구독 모달 표시
showSubscriptionModal(productId, authorId, authorName, monthlyPrice);

// 구독 취소 모달 표시
showCancelSubscriptionModal(subscriptionId, authorName);

// 구독 목록 로드
loadMySubscriptions(page);
```

### 6.2 필요한 라이브러리
```html
<!-- SweetAlert2 -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<!-- CSS 파일들 -->
<link rel="stylesheet" href="/css/subscription.css">
<script src="/js/subscription.js"></script>
```

---

## 7. 데이터베이스 스키마

### 7.1 Subscription 테이블
```sql
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscriber_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    monthly_price INT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    next_billing_date DATETIME NOT NULL,
    auto_renewal BOOLEAN DEFAULT TRUE,
    cancelled_at DATETIME,
    cancel_reason VARCHAR(500),
    reg_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 7.2 Payment 테이블
```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    method VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_date DATETIME,
    description VARCHAR(500),
    transaction_id VARCHAR(100),
    reg_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 8. 테스트 시나리오

### 8.1 구독 플로우 테스트
1. 사용자 로그인
2. 작품 상세 페이지에서 [구독하기] 클릭
3. 구독 모달에서 결제 정보 확인
4. [구독하기] 클릭하여 API 호출
5. 구독 성공 확인
6. 구독 목록에서 상태 확인

### 8.2 구독 취소 플로우 테스트
1. 구독 목록에서 [구독 취소] 클릭
2. 취소 사유 선택
3. [구독 취소] 클릭하여 API 호출
4. 구독 취소 성공 확인
5. 구독 상태 변경 확인

### 8.3 크론잡 테스트
1. 구독 만료일 설정
2. 크론잡 실행
3. 자동 갱신/만료 처리 확인
4. 로그 확인