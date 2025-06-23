// 우클릭 방지 스크립트
document.addEventListener('contextmenu', event => event.preventDefault());

// 자동화된 브라우저(봇) 감지 (Selenium, Puppeteer 등)
if (navigator.webdriver) {
    console.warn("자동화된 브라우저(봇) 사용이 감지되었습니다. 사이트 접근을 차단합니다.");

    // DOM이 로드되면 페이지의 모든 내용을 지우고 경고 메시지를 표시합니다.
    document.addEventListener('DOMContentLoaded', () => {
        document.body.innerHTML = `
            <div style="text-align: center; padding: 50px; font-family: sans-serif; color: #cc0000;">
                <h1 style="font-size: 24px;">비정상적인 접근 감지</h1>
                <p style="font-size: 16px;">자동화된 도구(봇)를 이용한 접근은 허용되지 않습니다.</p>
                <p style="font-size: 14px;">문제가 지속되면 관리자에게 문의해주세요.</p>
            </div>
        `;
    });
} 