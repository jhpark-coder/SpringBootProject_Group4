# 주문 도메인 통합 테스트 시나리오

## 테스트 환경 설정
- 서버 URL: http://localhost:8080
- 실제 돈이 나가지 않는 시뮬레이션 환경
- H2 인메모리 데이터베이스 사용

## 시나리오 1: 포인트 충전 테스트

### 1-1. 포인트 충전 요청
```bash
POST http://localhost:8080/api/orders/charge-points
Content-Type: application/json

{
  "memberId": 1,
  "amount": 10000,
  "paymentMethod": "CARD"
}
```

### 1-2. 예상 응답
```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "memberId": 1,
    "orderType": "POINT_CHARGE",
    "totalAmount": 10000,
    "status": "COMPLETED",
    "orderItems": [
      {
        "id": 1,
        "productId": null,
        "productName": "포인트 충전",
        "quantity": 1,
        "unitPrice": 10000,
        "totalPrice": 10000
      }
    ],
    "payment": {
      "id": 1,
      "paymentMethod": "CARD",
      "amount": 10000,
      "status": "COMPLETED",
      "transactionId": "TXN_20241201_001"
    },
    "createdAt": "2024-12-01T10:00:00"
  }
}
```

## 시나리오 2: 포인트로 상품 구매 테스트

### 2-1. 상품 구매 요청
```bash
POST http://localhost:8080/api/orders/purchase-with-points
Content-Type: application/json

{
  "memberId": 1,
  "productId": 1,
  "quantity": 2
}
```

### 2-2. 예상 응답
```json
{
  "success": true,
  "data": {
    "orderId": 2,
    "memberId": 1,
    "orderType": "PRODUCT_PURCHASE",
    "totalAmount": 6000,
    "status": "COMPLETED",
    "orderItems": [
      {
        "id": 2,
        "productId": 1,
        "productName": "프리미엄 콘텐츠",
        "quantity": 2,
        "unitPrice": 3000,
        "totalPrice": 6000
      }
    ],
    "payment": {
      "id": 2,
      "paymentMethod": "POINTS",
      "amount": 6000,
      "status": "COMPLETED",
      "transactionId": "TXN_20241201_002"
    },
    "createdAt": "2024-12-01T10:05:00"
  }
}
```

## 시나리오 3: 포인트 잔액 조회 테스트

### 3-1. 포인트 잔액 조회
```bash
GET http://localhost:8080/api/orders/points/balance?memberId=1
```

### 3-2. 예상 응답
```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "balance": 4000,
    "lastUpdated": "2024-12-01T10:05:00"
  }
}
```

## 시나리오 4: 주문 내역 조회 테스트

### 4-1. 주문 내역 조회
```bash
GET http://localhost:8080/api/orders/member/1
```

### 4-2. 예상 응답
```json
{
  "success": true,
  "data": [
    {
      "orderId": 1,
      "orderType": "POINT_CHARGE",
      "totalAmount": 10000,
      "status": "COMPLETED",
      "createdAt": "2024-12-01T10:00:00"
    },
    {
      "orderId": 2,
      "orderType": "PRODUCT_PURCHASE",
      "totalAmount": 6000,
      "status": "COMPLETED",
      "createdAt": "2024-12-01T10:05:00"
    }
  ]
}
```

## 시나리오 5: 구독 생성 테스트

### 5-1. 구독 생성 요청
```bash
POST http://localhost:8080/api/orders/subscriptions
Content-Type: application/json

{
  "memberId": 1,
  "productId": 2,
  "billingCycle": "MONTHLY",
  "paymentMethod": "CARD"
}
```

### 5-2. 예상 응답
```json
{
  "success": true,
  "data": {
    "orderId": 3,
    "memberId": 1,
    "orderType": "SUBSCRIPTION",
    "totalAmount": 15000,
    "status": "COMPLETED",
    "orderItems": [
      {
        "id": 3,
        "productId": 2,
        "productName": "월간 구독 서비스",
        "quantity": 1,
        "unitPrice": 15000,
        "totalPrice": 15000
      }
    ],
    "payment": {
      "id": 3,
      "paymentMethod": "CARD",
      "amount": 15000,
      "status": "COMPLETED",
      "transactionId": "TXN_20241201_003"
    },
    "subscription": {
      "id": 1,
      "billingCycle": "MONTHLY",
      "nextBillingDate": "2025-01-01T10:10:00",
      "status": "ACTIVE"
    },
    "createdAt": "2024-12-01T10:10:00"
  }
}
```

## 시나리오 6: 주문 취소 테스트

### 6-1. 주문 취소 요청
```bash
POST http://localhost:8080/api/orders/2/cancel
Content-Type: application/json

{
  "memberId": 1,
  "reason": "구매 의사 변경"
}
```

### 6-2. 예상 응답
```json
{
  "success": true,
  "data": {
    "orderId": 2,
    "status": "CANCELLED",
    "cancelledAt": "2024-12-01T10:15:00",
    "refundAmount": 6000,
    "refundMethod": "POINTS"
  }
}
```

## 시나리오 7: 결제 웹훅 테스트

### 7-1. 결제 완료 웹훅 시뮬레이션
```bash
POST http://localhost:8080/api/orders/payment-webhook
Content-Type: application/json

{
  "transactionId": "TXN_20241201_001",
  "status": "COMPLETED",
  "amount": 10000,
  "paymentMethod": "CARD",
  "timestamp": "2024-12-01T10:00:00"
}
```

### 7-2. 예상 응답
```json
{
  "success": true,
  "message": "Payment webhook processed successfully"
}
```

## 테스트 실행 순서 권장사항

1. **포인트 충전** (시나리오 1) - 먼저 포인트를 충전
2. **포인트 잔액 조회** (시나리오 3) - 충전 확인
3. **상품 구매** (시나리오 2) - 포인트로 상품 구매
4. **주문 내역 조회** (시나리오 4) - 구매 내역 확인
5. **구독 생성** (시나리오 5) - 구독 서비스 가입
6. **주문 취소** (시나리오 6) - 구매한 상품 취소
7. **결제 웹훅** (시나리오 7) - 외부 결제 시스템 연동 시뮬레이션

## curl 명령어 예시

### 포인트 충전
```bash
curl -X POST http://localhost:8080/api/orders/charge-points \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "amount": 10000,
    "paymentMethod": "CARD"
  }'
```

### 포인트 잔액 조회
```bash
curl -X GET "http://localhost:8080/api/orders/points/balance?memberId=1"
```

### 상품 구매
```bash
curl -X POST http://localhost:8080/api/orders/purchase-with-points \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

## 주의사항

1. **실제 돈이 나가지 않음**: 모든 결제는 시뮬레이션으로 처리됩니다.
2. **데이터 초기화**: 애플리케이션 재시작 시 H2 데이터베이스가 초기화됩니다.
3. **테스트 데이터**: DataInitializer에서 기본 테스트 데이터가 생성됩니다.
4. **에러 처리**: 잘못된 요청 시 적절한 에러 메시지가 반환됩니다.

## 예상 에러 케이스

### 포인트 부족 시
```json
{
  "success": false,
  "error": "Insufficient points balance",
  "message": "포인트 잔액이 부족합니다. 현재 잔액: 4000, 필요 금액: 10000"
}
```

### 존재하지 않는 상품 구매 시
```json
{
  "success": false,
  "error": "Product not found",
  "message": "상품을 찾을 수 없습니다. productId: 999"
}
```

### 잘못된 주문 취소 시
```json
{
  "success": false,
  "error": "Order cannot be cancelled",
  "message": "이미 완료된 주문은 취소할 수 없습니다."
}
``` 