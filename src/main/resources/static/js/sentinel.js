/**
 * Sentinel Script (감시 스크립트) - Dead Man's Switch 방식
 *
 * 이 스크립트는 보이지 않는 iframe 내에서 실행되며,
 * 부모 창(사용자가 보는 메인 페이지)을 감시하고 보호하는 역할을 합니다.
 */
(function () {
    'use strict';

    const parentWindow = window.parent;
    if (!parentWindow || parentWindow === window) {
        return; // 부모 창이 없거나 자기 자신이 부모이면 중단
    }

    /**
     * 임무 1 & 2: 우클릭 및 개발자 도구 단축키 차단 (alert 없이 조용히)
     */
    parentWindow.document.addEventListener('contextmenu', function (e) {
        e.preventDefault();
    }, false);

    parentWindow.document.addEventListener('keydown', function (e) {
        if (e.key === 'F12' || (e.ctrlKey && e.shiftKey && e.key === 'I') || (e.ctrlKey && e.key === 's')) {
            e.preventDefault();
        }
    });

    /**
     * 임무 3: JS 비활성화를 감지하기 위한 '죽은 자의 스위치'
     */
    let deathTimer; // 폭탄 타이머

    // 콘텐츠를 숨기는 함수
    function hideContent() {
        try {
            if (parentWindow.document.body) {
                parentWindow.document.body.dataset.sentinelState = 'dead';
            }
        } catch (e) { /* 부모 창 접근 불가 시 무시 */ }
    }

    // 1초마다 심장박동을 울려서, 폭탄 타이머를 계속 리셋하는 함수
    function heartbeat() {
        clearTimeout(deathTimer);
        deathTimer = setTimeout(hideContent, 2000); // 2초 내 응답 없으면 죽음으로 간주

        try {
            if (parentWindow.document.body) {
                parentWindow.document.body.dataset.sentinelState = 'alive'; // 살아있다는 신호 전송
            }
        } catch (e) {
            // 부모 창 접근 불가 에러가 발생해도 인터벌을 멈추지 않습니다.
        }
    }

    // 부모 창의 body가 렌더링될 때까지 기다렸다가 심장박동을 시작하는 함수
    function startWhenReady() {
        if (!parentWindow.document.body) {
            // body가 아직 없으면, 브라우저의 다음 페인트 주기에 다시 시도
            parentWindow.requestAnimationFrame(startWhenReady);
        } else {
            // body가 준비되었으므로, 심장박동을 즉시 시작하고 1초 간격으로 반복
            heartbeat();
            const heartbeatInterval = setInterval(heartbeat, 1000);

            // 인터벌 에러 시 clearInterval을 호출하지 않도록 try-catch 재정의
            try {
                // 이 try 블록은 setInterval이 반환한 ID를 클로저로 잡고 있습니다.
                // 부모 창 탐색 에러가 발생해도, 지역 heartbeatInterval 변수는 영향을 받지 않습니다.
            } catch(e) {
                 // 여기서 clearInterval을 호출하면 안 됩니다.
            }
        }
    }

    // 부모 창의 렌더링이 준비되기를 기다렸다가 로직을 시작
    startWhenReady();

})(); 