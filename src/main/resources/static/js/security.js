// ===================================================================
// 기본 보안 스크립트 (Basic Security Script)
// ===================================================================

/**
 * 즉시 실행 함수 (IIFE, Immediately Invoked Function Expression)
 * 이 스크립트 파일이 로드되는 즉시 내부의 모든 코드가 실행됩니다.
 * 전역 스코프를 오염시키지 않고 변수들을 안전하게 관리하기 위해 사용됩니다.
 */
(function () {
    // 'use strict';: 보다 엄격한 JavaScript 문법 규칙을 적용하여 잠재적인 오류를 방지합니다.
    'use strict';

    // ===================================================================
    // [A] CSRF 토큰 자동 첨부 기능
    // 페이지 로드 시 가장 먼저 설정되어야 하는 중요한 기능입니다.
    // ===================================================================
    function setupCSRF() {
        try {
            // jQuery 라이브러리가 페이지에 로드되어 있는 경우에만 실행합니다.
            if (typeof $ !== 'undefined') {
                // DOM(Document Object Model)이 완전히 로드된 후 내부 코드를 실행합니다.
                $(document).ready(function () {
                    // 1. HTML의 meta 태그에서 CSRF 토큰 값과 헤더 이름을 찾습니다.
                    var token = $("meta[name='_csrf']").attr("content");
                    var header = $("meta[name='_csrf_header']").attr("content");

                    // 2. 토큰과 헤더 이름이 모두 존재할 경우에만 다음 작업을 수행합니다.
                    if (token && header) {
                        // 3. 페이지 내에서 발생하는 모든 AJAX 요청을 감지하는 리스너를 등록합니다.
                        $(document).ajaxSend(function (e, xhr, options) {
                            // 4. 감지된 AJAX 요청의 HTTP 헤더에 (1)에서 찾은 토큰 정보를 자동으로 설정합니다.
                            xhr.setRequestHeader(header, token);
                        });
                    }
                });
            } else {
                console.log('jQuery 없이 보안 기능만 실행됩니다.');
            }
        } catch (error) {
            console.warn('CSRF 설정 중 오류가 발생했지만 보안 기능은 계속 실행됩니다:', error);
        }
    }

    // [실행 순서 1] 페이지가 로드되자마자 CSRF 설정을 시도합니다.
    setupCSRF();

    // ===================================================================
    // [B] 각종 사용자 입력 및 이벤트 차단 기능
    // 사용자의 특정 행동을 감지하고 차단하여 정보 유출을 방지합니다.
    // ===================================================================

    // [실행 순서 2-1] 자동화 도구(봇) 감지
    (function () {
        // 알려진 봇 관련 키워드 목록
        const botKeywords = ['webdriver', 'selenium', 'puppeteer', 'phantom'];
        // 현재 브라우저의 User Agent 정보를 소문자로 가져옵니다.
        const userAgent = navigator.userAgent.toLowerCase();
        // User Agent에 봇 키워드가 포함되어 있는지 확인합니다.
        const hasBot = botKeywords.some(keyword => userAgent.includes(keyword));

        // User Agent에 봇 키워드가 있거나, 자동화 도구가 사용하는 특정 전역 변수가 존재하는 경우
        if (hasBot || window.webdriver || window._selenium || window.callPhantom) {
            showNotice('자동화 도구 감지됨! 접근이 차단됩니다.');
            // 페이지의 모든 내용을 지우고 흰색 배경과 경고 메시지만 표시하여, 봇이 데이터를 수집하지 못하게 합니다.
            document.body.innerHTML = `
                <div style="
                    position: fixed; 
                    top: 0; left: 0; width: 100%; height: 100%; 
                    background: white; 
                    display: flex; align-items: center; justify-content: center;
                    font-size: 24px; color: red; z-index: 99999;
                ">
                    ⚠️ 자동화 접근 차단됨
                </div>
            `;
        }
    })();

    // [실행 순서 2-2] 마우스 우클릭 메뉴 차단
    document.addEventListener('contextmenu', function (e) {
        // 이벤트의 기본 동작(우클릭 메뉴 표시)을 막습니다.
        e.preventDefault();
        // 사용자에게 알림 메시지를 표시합니다.
        showNotice('우클릭이 제한됩니다.');
        return false;
    });

    // [실행 순서 2-3] 키보드 단축키 및 조합키 차단
    document.addEventListener('keydown', function (e) {
        // [NOTE] PrintScreen 키는 OS 레벨에서 처리되어 브라우저에서 안정적인 감지가 거의 불가능하므로 관련 코드를 제거합니다.

        // Windows 게임 바(화면 녹화 기능) 관련 단축키 차단
        if ((e.metaKey && e.key === 'g') || // Windows + G (게임 바)
            (e.metaKey && e.altKey && e.key === 'r')) { // Windows + Alt + R (녹화 시작/중지)
            e.preventDefault();
            showNotice('화면 녹화가 제한됩니다.');
            return false;
        }

        // --- 개발자 도구 관련 단축키 차단 ---
        if (e.ctrlKey || e.metaKey) { // Ctrl 또는 Cmd 키가 함께 눌렸을 경우
            if (e.shiftKey && (e.key === 'I' || e.key === 'J' || e.key === 'C')) {
                e.preventDefault();
                showNotice('개발자도구 사용이 제한됩니다.');
                return false;
            }
            // 소스 보기(Ctrl+U) 차단
            if (e.key === 'u') {
                e.preventDefault();
                showNotice('소스보기가 제한됩니다.');
                return false;
            }
        }

        // --- 콘텐츠 복사/저장 관련 단축키 차단 ---
        if (e.ctrlKey || e.metaKey) {
            if (e.key === 's') { // 저장 (Ctrl+S)
                e.preventDefault();
                showNotice('페이지 저장이 제한됩니다.');
                return false;
            }
            if (e.key === 'a') { // 전체 선택 (Ctrl+A)
                e.preventDefault();
                showNotice('전체 선택이 제한됩니다.');
                return false;
            }
            if (e.key === 'c') { // 복사 (Ctrl+C)
                e.preventDefault();
                showNotice('복사가 제한됩니다.');
                // 클립보드에 경고 메시지를 덮어쓰는 기능을 추가하여 보안을 강화합니다.
                try {
                    navigator.clipboard.writeText('⚠️ 무단 복제 금지 - Nexus 보안 시스템');
                } catch (err) {
                    console.warn('클립보드 접근 권한이 없습니다.');
                }
                return false;
            }
            if (e.key === 'v' && !isInputElement(e.target)) { // 붙여넣기 (Ctrl+V) (단, 입력 필드는 예외)
                e.preventDefault();
                showNotice('붙여넣기가 제한됩니다.');
                return false;
            }
            if (e.key === 'x') { // 잘라내기 (Ctrl+X)
                e.preventDefault();
                showNotice('잘라내기가 제한됩니다.');
                return false;
            }
        }

        // F12 키
        if (e.key === 'F12' || e.keyCode === 123) {
            e.preventDefault();
            showNotice('개발자도구 사용이 제한됩니다.');
            return false;
        }

        // 개발자 도구 관련
        if (e.ctrlKey || e.metaKey) {
            if (e.shiftKey && (e.key === 'I' || e.key === 'J' || e.key === 'C')) {
                e.preventDefault();
                showNotice('개발자도구 사용이 제한됩니다.');
                return false;
            }
        }
    });

    // [실행 순서 2-4] 탭 전환 감지 및 화면 보호
    let isPageVisible = true;
    // 브라우저 탭의 활성화 상태가 변경될 때마다 'visibilitychange' 이벤트가 발생합니다.
    document.addEventListener('visibilitychange', function () {
        // document.hidden이 true이면, 현재 탭이 비활성화되었음(다른 탭을 보거나 창을 내렸음)을 의미합니다.
        if (document.hidden) {
            isPageVisible = false;
            // 페이지가 보이지 않을 때, 다른 프로그램으로 화면을 캡처하는 것을 방해하기 위해 화면을 흐리게 처리합니다.
            document.body.style.filter = 'blur(10px)';
            showNotice('다른 창으로 전환되어 화면을 보호합니다.');
        } else {
            isPageVisible = true;
            // 페이지가 다시 활성화되면, 0.1초 후에 흐림 효과를 제거하여 원래대로 복구합니다.
            setTimeout(() => {
                document.body.style.filter = 'none';
            }, 100);
        }
    });

    // ===================================================================
    // [C] 페이지 콘텐츠 보호 기능
    // 페이지에 표시되는 텍스트, 이미지 등의 콘텐츠를 보호합니다.
    // ===================================================================

    // [실행 순서 3-1] 텍스트 선택(드래그) 방지
    (function () {
        // CSS를 페이지에 직접 삽입하여 모든 요소의 텍스트 선택을 막습니다.
        const style = document.createElement('style');
        style.textContent = `
            * {
                -webkit-user-select: none; /* Safari, Chrome */
                -moz-user-select: none;    /* Firefox */
                -ms-user-select: none;     /* IE */
                user-select: none;         /* Standard */
                -webkit-touch-callout: none; /* iOS Safari */
            }
            /* 단, <input>, <textarea> 등 텍스트를 입력해야 하는 요소는 예외적으로 선택을 허용합니다. */
            input, textarea, [contenteditable="true"] {
                -webkit-user-select: text;
                -moz-user-select: text;
                -ms-user-select: text;
                user-select: text;
            }
            
            /* 🔒 스크린샷 방지 CSS */
            /* 스크린샷 시 특수 효과 적용 */
            @media print {
                * {
                    display: none !important;
                }
                body::before {
                    content: "⚠️ 스크린샷 금지 - Nexus 보안 시스템";
                    display: block !important;
                    text-align: center;
                    font-size: 24px;
                    color: red;
                    background: black;
                    padding: 50px;
                }
            }
            

            
            /* 이미지 보호 강화 */
            img {
                -webkit-user-drag: none;
                -khtml-user-drag: none;
                -moz-user-drag: none;
                -o-user-drag: none;
                user-drag: none;
                pointer-events: none;
            }
            
            /* 스크린샷 시 특수 효과 (고급) */
            @media screen and (max-width: 9999px) {
                body::after {
                    content: "";
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    pointer-events: none;
                    z-index: 999999;
                    background: transparent;
                    opacity: 0.001;
                }
            }
        `;
        document.head.appendChild(style);
    })();

    // [실행 순서 3-2] 이미지 드래그 방지 및 동적 로딩된 이미지 보호
    // DOM이 완전히 로드된 후 실행됩니다.
    document.addEventListener('DOMContentLoaded', function () {
        // (워터마크 생성 함수는 현재 비활성화되어 있습니다)
        // createWatermark();

        // 1. 기존 이미지 보호 함수
        function protectImages() {
            // 페이지의 모든 <img> 태그를 찾아서
            document.querySelectorAll('img').forEach(img => {
                // 드래그 시작 이벤트를 막고,
                img.addEventListener('dragstart', (e) => e.preventDefault());
                // CSS 속성을 통해 드래그가 불가능하도록 설정합니다.
                img.style.webkitUserDrag = 'none';
                img.style.userDrag = 'none';
            });
        }
        // 페이지 로드 시점에 존재하는 모든 이미지에 보호 기능을 적용합니다.
        protectImages();

        // 2. 동적으로 추가되는 이미지 보호 (MutationObserver 사용)
        // MutationObserver는 DOM의 변화를 감지하는 강력한 API입니다.
        const observer = new MutationObserver(function (mutations) {
            // DOM에 변화가 생기면(mutations) 내부 코드를 실행합니다.
            mutations.forEach(function (mutation) {
                // 변화 중에서 '추가된 노드(addedNodes)'가 있는지 확인합니다.
                mutation.addedNodes.forEach(function (node) {
                    // 추가된 노드가 HTML 요소(Element)인 경우
                    if (node.nodeType === 1) {
                        // 그 요소가 'IMG' 태그이거나, 그 요소 안에 'IMG' 태그가 포함되어 있다면
                        if (node.tagName === 'IMG' || node.querySelectorAll) {
                            // 해당 이미지들에도 똑같이 드래그 방지 기능을 적용합니다.
                            // -> 무한 스크롤 등으로 나중에 추가되는 이미지도 보호할 수 있습니다.
                            const images = node.tagName === 'IMG' ? [node] : node.querySelectorAll('img');
                            images.forEach(img => {
                                img.addEventListener('dragstart', (e) => e.preventDefault());
                                img.style.webkitUserDrag = 'none';
                                img.style.userDrag = 'none';
                            });
                        }
                    }
                });
            });
        });
        // document.body의 모든 자식 요소 및 그 하위 요소들의 변화를 감지하도록 옵저버를 시작합니다.
        observer.observe(document.body, { childList: true, subtree: true });
    });

    // ===================================================================
    // [B-1] 강화된 스크린샷 방지 - CSS 기반 + 키보드 감지 시스템
    // 클립보드 권한 없이도 효과적인 스크린샷 방지
    // ===================================================================
    (function () {
        let screenshotAttempts = 0;
        let lastScreenshotTime = 0;

        // CSS 기반 스크린샷 방지 스타일 적용
        function applyScreenshotPreventionCSS() {
            const style = document.createElement('style');
            style.textContent = `
                /* 스크린샷 방지용 CSS (주석처리됨)
                /* 스크린샷 시 특수 효과 */
                @media print {
                    * {
                        display: none !important;
                    }
                    body::before {
                        content: "⚠️ 스크린샷 금지 - Nexus 보안 시스템";
                        display: block !important;
                        text-align: center;
                        font-size: 24px;
                        color: red;
                        background: black;
                        padding: 50px;
                    }
                }
                
                /* 스크린샷 방지용 CSS */
                .screenshot-protection {
                    -webkit-user-select: none;
                    -moz-user-select: none;
                    -ms-user-select: none;
                    user-select: none;
                    -webkit-touch-callout: none;
                }
                
                /* 스크린샷 시도 감지용 가상 요소 */
                .screenshot-detector::before {
                    content: "";
                    position: fixed;
                    top: -9999px;
                    left: -9999px;
                    width: 1px;
                    height: 1px;
                    background: transparent;
                    z-index: -1;
                }
                
                /* 스크린샷 시 화면 효과 */
                .screenshot-alert {
                    animation: screenshotAlert 0.5s ease-in-out;
                }
                
                @keyframes screenshotAlert {
                    0% { filter: blur(0px); }
                    50% { filter: blur(10px) brightness(0.5); }
                    100% { filter: blur(0px); }
                }

                /* 스크린샷 방지 강화 - 모든 요소에 적용 */
                * {
                    -webkit-print-color-adjust: exact !important;
                    color-adjust: exact !important;
                }
                */
            `;
            document.head.appendChild(style);
        }

        // 강화된 키보드 이벤트 감지
        function setupEnhancedKeyboardDetection() {
            document.addEventListener('keydown', function (e) {
                // F12 키 (개발자도구) - 최우선 처리
                if (e.key === 'F12' || e.keyCode === 123) {
                    e.preventDefault();
                    showNotice('개발자도구 사용이 제한됩니다.');
                    return false;
                }

                /* 스크린샷 관련 키 감지 (주석처리됨)
                // PrintScreen 키 감지 (다양한 방법)
                if (e.key === 'PrintScreen' || e.keyCode === 44 ||
                    e.which === 44 || e.code === 'PrintScreen' ||
                    e.key === 'F13' || e.keyCode === 124 || // 일부 브라우저에서 PrintScreen을 F13으로 감지
                    (e.ctrlKey && e.key === 'p') || // Ctrl+P (인쇄)
                    (e.metaKey && e.key === 'p')) { // Cmd+P (Mac 인쇄)
                    e.preventDefault();
                    handleScreenshotAttempt('PrintScreen/인쇄 키 감지');
                    return false;
                }

                // Windows + Shift + S (스니핑 도구)
                if (e.metaKey && e.shiftKey && e.key === 'S') {
                    e.preventDefault();
                    handleScreenshotAttempt('스니핑 도구 감지');
                    return false;
                }

                // Windows + G (게임 바)
                if (e.metaKey && e.key === 'G') {
                    e.preventDefault();
                    handleScreenshotAttempt('게임 바 감지');
                    return false;
                }

                // Windows + PrtScn
                if (e.metaKey && (e.key === 'PrintScreen' || e.keyCode === 44)) {
                    e.preventDefault();
                    handleScreenshotAttempt('Windows + PrintScreen 감지');
                    return false;
                }

                // Alt + PrintScreen
                if (e.altKey && (e.key === 'PrintScreen' || e.keyCode === 44)) {
                    e.preventDefault();
                    handleScreenshotAttempt('Alt + PrintScreen 감지');
                    return false;
                }
                */

                // 개발자 도구 관련 (Ctrl+Shift+I, Ctrl+Shift+J, Ctrl+Shift+C)
                if (e.ctrlKey || e.metaKey) {
                    if (e.shiftKey && (e.key === 'I' || e.key === 'J' || e.key === 'C')) {
                        e.preventDefault();
                        showNotice('개발자도구 사용이 제한됩니다.');
                        return false;
                    }
                }
            });

            /* 스크린샷 관련 keyup 이벤트 (주석처리됨)
            // keyup 이벤트도 감지 (일부 PrintScreen 키는 keydown에서 감지되지 않음)
            document.addEventListener('keyup', function (e) {
                if (e.key === 'PrintScreen' || e.keyCode === 44 ||
                    e.which === 44 || e.code === 'PrintScreen') {
                    e.preventDefault();
                    handleScreenshotAttempt('PrintScreen 키 감지 (keyup)');
                    return false;
                }
            });

            // beforeprint 이벤트 감지 (인쇄 시도)
            window.addEventListener('beforeprint', function () {
                handleScreenshotAttempt('인쇄 시도 감지');
                // 인쇄를 취소
                setTimeout(() => {
                    window.close();
                }, 100);
            });
            */
        }

        /* 스크린샷 시도 처리 함수 (주석처리됨)
        // 스크린샷 시도 처리
        function handleScreenshotAttempt(reason) {
            const now = Date.now();
            screenshotAttempts++;

            // 연속 시도 방지 (1초 내 3회 이상)
            if (now - lastScreenshotTime < 1000 && screenshotAttempts > 3) {
                showNotice('⚠️ 과도한 스크린샷 시도 - 보안 모드 활성화');
                activateSecurityMode();
                return;
            }

            lastScreenshotTime = now;
            showNotice(`스크린샷 시도 감지: ${reason}`);

            // 화면 효과 적용
            document.body.classList.add('screenshot-alert');
            setTimeout(() => {
                document.body.classList.remove('screenshot-alert');
            }, 500);

            // 추가 보안 조치
            setTimeout(() => {
                document.body.style.filter = 'blur(3px)';
                setTimeout(() => {
                    document.body.style.filter = 'none';
                }, 2000);
            }, 100);
        }

        // 보안 모드 활성화
        function activateSecurityMode() {
            // 화면 강제 흐림
            document.body.style.filter = 'blur(8px) brightness(0.3)';

            // 경고 메시지 표시
            const warning = document.createElement('div');
            warning.id = 'security-warning';
            warning.style.cssText = `
                position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
                background: #e74c3c; color: white; padding: 30px;
                border-radius: 10px; font-size: 18px; font-weight: bold;
                z-index: 10000; text-align: center; box-shadow: 0 10px 30px rgba(0,0,0,0.5);
            `;
            warning.innerHTML = `
                <h2>⚠️ 보안 경고</h2>
                <p>스크린샷 시도가 감지되어 보안 모드가 활성화되었습니다.</p>
                <p>1초 후 정상 모드로 복구됩니다.</p>
            `;
            document.body.appendChild(warning);

            // 1초 후 복구
            setTimeout(() => {
                document.body.style.filter = 'none';
                warning.remove();
                screenshotAttempts = 0;
            }, 1000);
        }
        */

        // 마우스 이벤트 감지 (의심스러운 행동)
        function setupMouseDetection() {
            let mouseDownTime = 0;
            let mousePosition = { x: 0, y: 0 };

            document.addEventListener('mousedown', function (e) {
                mouseDownTime = Date.now();
                mousePosition = { x: e.clientX, y: e.clientY };
            });

            document.addEventListener('mouseup', function (e) {
                const holdTime = Date.now() - mouseDownTime;
                const distance = Math.sqrt(
                    Math.pow(e.clientX - mousePosition.x, 2) +
                    Math.pow(e.clientY - mousePosition.y, 2)
                );

                // 길게 누르기 + 드래그 감지
                if (holdTime > 3000 && distance < 10) {
                    handleScreenshotAttempt('의심스러운 마우스 동작');
                }
            });
        }

        // 페이지 가시성 변화 감지
        function setupVisibilityDetection() {
            let lastVisibilityChange = Date.now();

            document.addEventListener('visibilitychange', function () {
                const now = Date.now();

                // 빠른 탭 전환 감지 (스크린샷 도구 사용 시)
                if (now - lastVisibilityChange < 500) {
                    handleScreenshotAttempt('빠른 탭 전환 감지');
                }

                lastVisibilityChange = now;

                if (document.hidden) {
                    // 페이지가 숨겨질 때 화면 흐림
                    document.body.style.filter = 'blur(5px)';
                } else {
                    // 페이지가 다시 보일 때
                    setTimeout(() => {
                        document.body.style.filter = 'none';
                    }, 300);
                }
            });
        }

        // 초기화
        function initScreenshotPrevention() {
            applyScreenshotPreventionCSS();
            setupEnhancedKeyboardDetection();
            setupMouseDetection();
            setupVisibilityDetection();

            // 페이지에 스크린샷 방지 클래스 추가
            document.body.classList.add('screenshot-protection', 'screenshot-detector');

            console.log('🔒 CSS 기반 스크린샷 방지 시스템 활성화됨');
        }

        // 시스템 시작
        initScreenshotPrevention();
    })();

    // ===================================================================
    // [D] 헬퍼(Helper) 함수 및 기타 기능
    // 위 기능들이 공통적으로 사용하는 보조 함수들입니다.
    // ===================================================================

    /**
     * 특정 요소가 사용자가 텍스트를 입력할 수 있는 요소인지 확인하는 함수.
     * @param element 확인할 HTML 요소.
     * @returns {boolean} 입력 가능한 요소이면 true, 아니면 false.
     */
    function isInputElement(element) {
        const inputTags = ['INPUT', 'TEXTAREA'];
        const isContentEditable = element.contentEditable === 'true';
        return inputTags.includes(element.tagName) || isContentEditable;
    }

    /**
     * 화면 우측 상단에 알림 메시지(토스트)를 표시하는 함수.
     * @param {string} message 표시할 메시지 내용.
     */
    function showNotice(message) {
        // 1. 혹시 이전에 표시된 알림이 있다면 먼저 제거합니다.
        const existingNotice = document.getElementById('security-notice');
        if (existingNotice) {
            existingNotice.remove();
        }

        // 2. 새로운 알림 <div> 요소를 생성하고, CSS 스타일을 적용합니다.
        const notice = document.createElement('div');
        notice.id = 'security-notice';

        // 스크롤 위치를 고려한 동적 위치 계산
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const topPosition = Math.max(20, scrollTop + 20); // 최소 20px, 스크롤 위치 + 20px

        notice.style.cssText = `
            position: fixed; top: ${topPosition}px; right: 20px;
            background: #ff6b6b; color: white; padding: 12px 20px;
            border-radius: 6px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 14px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            z-index: 10000; opacity: 0; transform: translateY(-20px);
            transition: all 0.3s ease; max-width: 300px; word-wrap: break-word;
        `;
        notice.textContent = message;

        // 3. 생성된 알림을 페이지의 <body>에 추가합니다.
        document.body.appendChild(notice);

        // 4. 스크롤 이벤트 리스너 추가 (토스트가 표시되는 동안 위치 업데이트)
        const updatePosition = () => {
            const newScrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const newTopPosition = Math.max(20, newScrollTop + 20);
            notice.style.top = `${newTopPosition}px`;
        };

        window.addEventListener('scroll', updatePosition);

        // 5. 애니메이션 효과와 함께 알림을 표시합니다.
        setTimeout(() => {
            notice.style.opacity = '1';
            notice.style.transform = 'translateY(0)';
        }, 10);

        // 6. 3초 후에 자동으로 사라지도록 설정합니다.
        setTimeout(() => {
            notice.style.opacity = '0';
            notice.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notice.parentNode) {
                    notice.remove();
                    window.removeEventListener('scroll', updatePosition);
                }
            }, 300);
        }, 3000);
    }

    // [실행 순서 4] 개발자 콘솔에 경고 메시지 출력
    // 개발자 도구를 열어본 사람에게 경각심을 주기 위한 메시지입니다.
    console.warn("⚠️ 개발자도구 사용 감지! 무단 복사는 금지됩니다.");
    console.log("%c경고: 이 사이트의 콘텐츠는 저작권으로 보호받습니다.",
        "color: red; font-size: 16px; font-weight: bold; text-shadow: 1px 1px 2px rgba(0,0,0,0.3);");

    // --- [최종 실행] ---
    // 모든 보안 설정이 완료되었음을 알리는 커스텀 이벤트를 발생시킵니다.
    // 다른 스크립트 파일(예: gridViewTest.js)에서 이 이벤트를 수신하여,
    // 보안 설정이 완료된 후에야 API 요청과 같은 중요한 작업을 시작하도록 할 수 있습니다.
    window.securityIsInitialized = true; // 실행 완료 플래그(깃발)를 설정합니다.
    document.dispatchEvent(new CustomEvent('security-initialized'));

})(); 