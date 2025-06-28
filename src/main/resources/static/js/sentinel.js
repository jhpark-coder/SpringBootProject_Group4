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
            parentWindow.document.body.dataset.sentinelState = 'dead';
        } catch (e) { /* 부모 창 접근 불가 시 무시 */ }
    }

    // 1초마다 심장박동을 울려서, 폭탄 타이머를 계속 리셋하는 함수
    function heartbeat() {
        clearTimeout(deathTimer);
        deathTimer = setTimeout(hideContent, 2000); // 2초 내 응답 없으면 죽음으로 간주

        try {
            parentWindow.document.body.dataset.sentinelState = 'alive'; // 살아있다는 신호 전송
        } catch (e) {
            clearInterval(heartbeatInterval);
        }
    }

    // 스크립트 로드 즉시 첫 신호를 보내 1초 딜레이를 없애고, 그 후 주기적으로 신호 전송
    heartbeat();
    const heartbeatInterval = setInterval(heartbeat, 1000);

})(); 