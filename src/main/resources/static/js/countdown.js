// resources/static/js/countdown.js

/**
 * 지정된 HTML 요소에 대한 카운트다운을 시작하고 업데이트합니다.
 * @param {HTMLElement} element - 카운트다운을 표시할 HTML 요소.
 */
function startCountdown(element) {
    // 요소가 없거나, 이미 카운트다운이 시작되었다면 즉시 종료
    if (!element || element.dataset.countdownInitialized) {
        return;
    }
    const endTime = new Date(element.dataset.endtime).getTime();
    element.dataset.countdownInitialized = 'true';
    // ==================== [수정된 부분 1] ====================
    // 즉시 남은 시간을 계산하고 표시하는 함수
    const updateTimer = () => {
        const now = new Date().getTime();
        const distance = endTime - now;

        if (distance < 0) {
            // 이따금 호출될 interval을 여기서 확실히 정리합니다.
            if (element.countdownInterval) {
                clearInterval(element.countdownInterval);
            }
            element.innerHTML = "경매 종료";
            element.style.color = "red";
            return false; // 타이머가 끝났음을 알림
        }

        const days = Math.floor(distance / (1000 * 60 * 60 * 24));
        const hours = String(Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))).padStart(2, '0');
        const minutes = String(Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, '0');
        const seconds = String(Math.floor((distance % (1000 * 60)) / 1000)).padStart(2, '0');

        element.innerHTML = `${days}일 ${hours}:${minutes}:${seconds}`;
        return true; // 타이머가 계속 실행 중임을 알림
    };

    // 1. 함수를 즉시 한 번 호출하여 '--일 --:--:--' 상태를 건너뜁니다.
    const isRunning = updateTimer();

    // 2. 타이머가 아직 실행 중일 때만 1초 간격으로 업데이트를 예약합니다.
    if (isRunning) {
        element.countdownInterval = setInterval(updateTimer, 1000);
    }
    // =======================================================
    // 타이머를 서서히 보이게 만듦
    element.style.opacity = '1';
}

// ==================== [수정된 부분] ====================
// 페이지 전체에서 'gridAnimationEnd' 이벤트가 발생하기를 기다림
document.addEventListener('gridAnimationEnd', function(event) {
    // 이벤트와 함께 전달된 부모 article 요소를 가져옴
    const parentArticle = event.detail.parentArticle;
    if (parentArticle) {
        // 그 article 안에서만 .countdown-timer를 찾아 실행
        const countdownElement = parentArticle.querySelector('.countdown-timer');
        startCountdown(countdownElement);
    }
});
// =======================================================
// ==================== [추가된 부분] ====================
/**
 * 페이지가 로드되었을 때, 애니메이션에 의존하지 않는 모든 카운트다운을 즉시 시작합니다.
 * 상세 페이지와 같이 정적인 페이지에서 타이머를 작동시키는 역할을 합니다.
 */
document.addEventListener('DOMContentLoaded', function() {
    // '.grid-item' 내부에 있지 않은 타이머만 찾습니다.
    // 이는 그리드 페이지의 타이머가 중복 실행되는 것을 방지합니다.
    const staticTimers = document.querySelectorAll('.countdown-timer:not(.grid-item .countdown-timer)');

    staticTimers.forEach(timer => {
        startCountdown(timer);
    });
});
// =======================================================