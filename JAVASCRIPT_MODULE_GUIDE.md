# NEXUS JavaScript 모듈 시스템 가이드

## 개요

기존의 개별 JavaScript 파일들을 통합 관리하는 새로운 모듈 시스템을 구축했습니다. 이 시스템은 다음과 같은 장점을 제공합니다:

- **의존성 관리**: 모듈 간 의존성을 명확히 정의하고 자동으로 해결
- **로딩 순서 제어**: 모듈 로딩 순서를 자동으로 관리
- **페이지별 최적화**: 필요한 모듈만 로드하여 성능 향상
- **전역 네임스페이스 보호**: 모듈 간 충돌 방지
- **이벤트 시스템**: 모듈 간 통신을 위한 이벤트 시스템 제공

## 시스템 구조

```
src/main/resources/static/js/
├── nexus-core.js          # 핵심 시스템 (모듈 관리, 이벤트 시스템)
├── modules/               # 모듈 디렉토리
│   ├── memberForm.js      # 회원가입 폼 모듈
│   ├── security.js        # 보안 모듈
│   ├── chat-buttons.js    # 채팅 버튼 모듈
│   ├── chat.js           # 채팅 기능 모듈
│   ├── notification-list.js # 알림 목록 모듈
│   ├── notification-badge.js # 알림 배지 모듈
│   ├── heartFollow.js     # 좋아요/팔로우 모듈
│   ├── main-slide.js      # 메인 슬라이드 모듈
│   ├── grid-animation.js  # 그리드 애니메이션 모듈
│   ├── grid-animation-auction.js # 경매 그리드 애니메이션 모듈
│   ├── countdown.js       # 카운트다운 모듈
│   └── memberModify.js    # 회원정보 수정 모듈
└── nexus-namespace.js     # 네임스페이스 정의 (기존)
```

## 핵심 컴포넌트

### 1. NEXUS Core (`nexus-core.js`)

모든 모듈을 관리하는 핵심 시스템입니다.

#### 주요 기능:
- **모듈 시스템**: 모듈 등록, 로드, 의존성 관리
- **이벤트 시스템**: 모듈 간 통신을 위한 이벤트 시스템
- **페이지 관리**: 페이지별 모듈 자동 로드
- **유틸리티**: 공통 유틸리티 함수들 (AJAX, 팝업, 스토리지 등)

#### 사용법:
```javascript
// 모듈 등록
NEXUS.core.modules.register('moduleName', function(deps) {
    // 모듈 코드
}, ['dependency1', 'dependency2']);

// 모듈 로드
NEXUS.core.modules.load('moduleName').then(function(module) {
    // 모듈 사용
});

// 이벤트 발생
NEXUS.core.events.emit('eventName', data);

// 이벤트 리스닝
NEXUS.core.events.on('eventName', function(data) {
    // 이벤트 처리
});
```

### 2. 모듈 구조

각 모듈은 다음과 같은 구조를 따릅니다:

```javascript
(function(NEXUS) {
    'use strict';

    var ModuleName = function(utils, eventSystem) {
        // 프라이빗 변수들
        var privateVar = 'value';

        // 프라이빗 함수들
        function privateFunction() {
            // 구현
        }

        // 공개 함수들
        function publicFunction() {
            // 구현
        }

        // 초기화 함수
        function init() {
            // 모듈 초기화 로직
        }

        // 공개 API 반환
        return {
            init: init,
            publicFunction: publicFunction
        };
    };

    // 모듈 등록
    NEXUS.core.modules.register('moduleName', function(utils, eventSystem) {
        return ModuleName(utils, eventSystem);
    }, ['utils', 'eventSystem']);

})(window.NEXUS);
```

## 페이지별 모듈 매핑

시스템은 현재 페이지를 자동으로 감지하고 필요한 모듈만 로드합니다:

### 메인 페이지
- `main-slide`: 메인 슬라이드 기능
- `heartFollow`: 좋아요/팔로우 기능

### 회원 관련 페이지
- `member-register`: `memberForm` 모듈
- `member-modify`: `memberModify` 모듈
- `member-mypage`: `heartFollow` 모듈

### 상품 관련 페이지
- `product-category`: `heartFollow`, `grid-animation`, `chat-buttons` 모듈
- `product-detail`: `heartFollow` 모듈

### 경매 관련 페이지
- `auction-grid`: `heartFollow`, `grid-animation-auction`, `chat-buttons`, `countdown` 모듈
- `auction-detail`: `heartFollow`, `countdown` 모듈

### 공통 모듈 (모든 페이지)
- `security`: 보안 기능
- `chat-buttons`: 채팅 버튼
- `chat`: 채팅 기능
- `notification-list`: 알림 목록
- `notification-badge`: 알림 배지

## 레이아웃 파일 수정

### 기존 방식 (문제점)
```html
<!-- 각 페이지마다 다른 순서로 스크립트 로드 -->
<script src="/js/chat-buttons.js"></script>
<script src="/js/chat.js"></script>
<script th:src="@{/js/security.js}"></script>
<script src="/js/notification-list.js"></script>
<script src="/js/notification-badge.js"></script>
```

### 새로운 방식 (해결책)
```html
<!-- NEXUS Core 시스템 -->
<script src="/js/nexus-core.js"></script>

<!-- 모듈 스크립트들 (한 번만 로드) -->
<script src="/js/modules/memberForm.js"></script>
<script src="/js/modules/security.js"></script>
<script src="/js/modules/chat-buttons.js"></script>
<!-- ... 기타 모듈들 -->

<!-- 페이지별 추가 스크립트 -->
<th:block layout:fragment="script"></th:block>
```

## 모듈 개발 가이드

### 1. 새 모듈 생성

1. `src/main/resources/static/js/modules/` 디렉토리에 새 파일 생성
2. 모듈 템플릿 구조 사용
3. `nexus-core.js`의 페이지 매핑에 추가

### 2. 의존성 관리

```javascript
// 의존성이 있는 모듈 등록
NEXUS.core.modules.register('myModule', function(utils, eventSystem, otherModule) {
    // otherModule을 사용하는 코드
}, ['utils', 'eventSystem', 'otherModule']);
```

### 3. 이벤트 시스템 활용

```javascript
// 이벤트 발생
eventSystem.emit('userLoggedIn', { userId: 123 });

// 이벤트 리스닝
eventSystem.on('userLoggedIn', function(data) {
    console.log('사용자 로그인:', data.userId);
});
```

### 4. 유틸리티 함수 사용

```javascript
// AJAX 요청
utils.ajax({
    url: '/api/data',
    type: 'POST',
    data: JSON.stringify(data)
}).then(function(response) {
    // 성공 처리
}).catch(function(error) {
    // 에러 처리
});

// 팝업 표시
utils.popup.show('메시지', {
    callback: function() {
        // 팝업 닫힐 때 실행
    }
});

// 로컬 스토리지
utils.storage.set('key', 'value');
var value = utils.storage.get('key');
```

## 마이그레이션 가이드

### 1. 기존 스크립트를 모듈로 변환

1. 기존 스크립트 파일을 `modules/` 디렉토리로 이동
2. 모듈 구조로 리팩토링
3. 전역 함수들을 모듈 API로 노출
4. 의존성 정의

### 2. HTML 파일 수정

1. 개별 스크립트 태그 제거
2. NEXUS Core 시스템 스크립트 추가
3. 모듈 스크립트들 추가

### 3. 전역 함수 호출 수정

```javascript
// 기존 방식
openPopup('메시지');

// 새로운 방식
NEXUS_POPUP.show('메시지');
// 또는
NEXUS.core.modules.load('memberForm').then(function(module) {
    module.openPopup('메시지');
});
```

## 디버깅 및 문제 해결

### 1. 콘솔 로그 확인

모듈 로딩 과정이 콘솔에 출력됩니다:
```
NEXUS Core 시스템 초기화 시작...
모듈 등록됨: memberForm
모듈 등록됨: security
현재 페이지: member-register
MemberForm 모듈 초기화...
Security 모듈 초기화...
```

### 2. 모듈 로딩 확인

```javascript
// 브라우저 콘솔에서 확인
console.log(NEXUS.core.modules);
console.log(NEXUS.core.pages.currentPage);
```

### 3. 이벤트 디버깅

```javascript
// 모든 이벤트 리스닝
NEXUS.core.events.on('*', function(eventName, data) {
    console.log('이벤트 발생:', eventName, data);
});
```

## 성능 최적화

### 1. 모듈 지연 로딩

필요한 시점에만 모듈을 로드:

```javascript
// 사용자가 버튼 클릭할 때만 모듈 로드
$('#someButton').on('click', function() {
    NEXUS.core.modules.load('heavyModule').then(function(module) {
        module.init();
    });
});
```

### 2. 조건부 모듈 로딩

페이지 조건에 따라 모듈 로드:

```javascript
if (window.location.pathname.includes('/admin')) {
    NEXUS.core.modules.load('adminModule');
}
```

## 주의사항

1. **모듈 순환 의존성**: 모듈 간 순환 의존성을 피하세요
2. **전역 변수 사용 금지**: 모듈 내에서 전역 변수를 직접 사용하지 마세요
3. **이벤트 네이밍**: 이벤트 이름을 명확하고 일관성 있게 지정하세요
4. **에러 처리**: 모듈 로딩 실패에 대한 적절한 에러 처리를 구현하세요

## 결론

이 새로운 모듈 시스템을 통해 JavaScript 코드의 관리가 훨씬 쉬워지고, 페이지 로딩 성능이 향상되며, 코드의 재사용성과 유지보수성이 크게 개선됩니다. 기존 코드를 점진적으로 마이그레이션하면서 시스템의 장점을 활용해보세요. 