// main.js (페이지네이션 기능이 추가된 최종 버전)
function setupSlider() {
    const slideSection = document.querySelector('.slideSection');
    if (!slideSection) return;

    const slideWrap = slideSection.querySelector('.slideWrap');
    const originalSlides = slideSection.querySelectorAll('.slide');
    const nextBtn = slideSection.querySelector('.btn-next');
    const prevBtn = slideSection.querySelector('.btn-prev');
    const pagination = slideSection.querySelector('.pagination'); // 페이지네이션 요소 선택

    if (!slideWrap || originalSlides.length === 0 || !nextBtn || !prevBtn || !pagination) {
        console.error('슬라이더 또는 페이지네이션에 필요한 요소가 HTML에 없습니다.');
        return;
    }

    const slideCount = originalSlides.length;
    let current = 0;
    let isMoving = false;
    const transitionTime = 500;

    // --- 페이지네이션 동적 생성 ---
    for (let i = 0; i < slideCount; i++) {
        const pageDot = document.createElement('a');
        pageDot.href = '#';
        pageDot.dataset.index = i; // 각 점에 인덱스 번호 저장
        pagination.appendChild(pageDot);
    }
    const pageDots = pagination.querySelectorAll('a');
    pageDots[0].classList.add('active'); // 첫 번째 점 활성화

    // 복제본 생성
    const firstEl = originalSlides[0].cloneNode(true);
    const lastEl = originalSlides[slideCount - 1].cloneNode(true);
    slideWrap.appendChild(firstEl);
    slideWrap.insertBefore(lastEl, slideWrap.firstElementChild);

    // 슬라이더 위치 계산 및 설정 함수 (이전과 동일)
    function setSliderPosition() {
        const containerWidth = slideSection.offsetWidth;
        const slideWidth = originalSlides[0].offsetWidth;
        const initialOffset = (containerWidth - slideWidth) / 2;
        const newLeft = initialOffset - slideWidth * (current + 1);
        slideWrap.style.left = `${newLeft}px`;
    }

    function initialize() {
        slideWrap.style.transition = 'none';
        setSliderPosition();
    }

    // --- 페이지네이션 업데이트 함수 ---
    function updatePagination() {
        pageDots.forEach(dot => dot.classList.remove('active'));
        // current 값이 -1 또는 slideCount가 되는 순간을 고려하여 실제 인덱스에만 적용
        if (current >= 0 && current < slideCount) {
            pageDots[current].classList.add('active');
        } else if (current === slideCount) { // 마지막 슬라이드에서 다음으로 넘어갈 때
            pageDots[0].classList.add('active');
        } else { // 첫 슬라이드에서 이전으로 넘어갈 때
            pageDots[slideCount - 1].classList.add('active');
        }
    }

    // 슬라이드 이동 함수 (페이지네이션 업데이트 호출 추가)
    function moveTo(targetIndex) {
        if (isMoving) return;
        isMoving = true;
        current = targetIndex;

        slideWrap.style.transition = `${transitionTime}ms ease-out`;
        setSliderPosition();
        updatePagination(); // 페이지네이션 업데이트

        setTimeout(() => { isMoving = false; }, transitionTime);
    }

    // 버튼 이벤트 리스너 (moveTo 함수 사용하도록 수정)
    nextBtn.addEventListener('click', () => {
        if (isMoving) return;
        current++;
        isMoving = true;
        slideWrap.style.transition = `${transitionTime}ms ease-out`;
        setSliderPosition();
        updatePagination();

        if (current === slideCount) {
            setTimeout(() => {
                slideWrap.style.transition = 'none';
                current = 0;
                setSliderPosition();
            }, transitionTime);
        }

        setTimeout(() => { isMoving = false; }, transitionTime);
    });

    prevBtn.addEventListener('click', () => {
        if (isMoving) return;
        current--;
        isMoving = true;
        slideWrap.style.transition = `${transitionTime}ms ease-out`;
        setSliderPosition();
        updatePagination();

        if (current === -1) {
            setTimeout(() => {
                slideWrap.style.transition = 'none';
                current = slideCount - 1;
                setSliderPosition();
            }, transitionTime);
        }

        setTimeout(() => { isMoving = false; }, transitionTime);
    });

    // --- 페이지네이션 클릭 이벤트 ---
    pagination.addEventListener('click', (e) => {
        e.preventDefault(); // a 태그의 기본 동작(페이지 이동) 방지
        const target = e.target;
        if (target.tagName === 'A') {
            const targetIndex = parseInt(target.dataset.index, 10);
            if (targetIndex !== current) {
                moveTo(targetIndex);
            }
        }
    });

    // 최초 실행 및 반응형 대응
    window.addEventListener('load', initialize);
    window.addEventListener('resize', initialize);
}

document.addEventListener('DOMContentLoaded', setupSlider);

//// main.js (최종 안정화 버전)
//function setupSlider() {
//    const slideSection = document.querySelector('.slideSection');
//    if (!slideSection) return;
//
//    const slideWrap = slideSection.querySelector('.slideWrap');
//    const originalSlides = slideSection.querySelectorAll('.slide');
//    const nextBtn = slideSection.querySelector('.btn-next');
//    const prevBtn = slideSection.querySelector('.btn-prev');
//
//    if (!slideWrap || originalSlides.length === 0 || !nextBtn || !prevBtn) {
//        console.error('슬라이더에 필요한 요소가 HTML에 없습니다.');
//        return;
//    }
//
//    const slideCount = originalSlides.length;
//    let current = 0;
//    let isMoving = false;
//    const transitionTime = 500; // 애니메이션 시간 변수화
//
//    const firstEl = originalSlides[0].cloneNode(true);
//    const lastEl = originalSlides[slideCount - 1].cloneNode(true);
//    slideWrap.appendChild(firstEl);
//    slideWrap.insertBefore(lastEl, slideWrap.firstElementChild);
//
//    // 슬라이더 위치 계산 및 설정 함수
//    function setSliderPosition() {
//        // slideSection의 너비를 기준으로 중앙 정렬 계산
//        const containerWidth = slideSection.offsetWidth;
//        const slideWidth = originalSlides[0].offsetWidth;
//        const initialOffset = (containerWidth - slideWidth) / 2;
//        const newLeft = initialOffset - slideWidth * (current + 1);
//        slideWrap.style.left = `${newLeft}px`;
//    }
//
//    function initialize() {
//        slideWrap.style.transition = 'none';
//        setSliderPosition();
//    }
//
//    // 버튼 이벤트 리스너
//    nextBtn.addEventListener('click', () => moveAndCheckClone(true));
//    prevBtn.addEventListener('click', () => moveAndCheckClone(false));
//
//    function moveAndCheckClone(isNext) {
//        if (isMoving) return;
//        isMoving = true;
//
//        current += isNext ? 1 : -1;
//
//        slideWrap.style.transition = `${transitionTime}ms ease-out`;
//        setSliderPosition();
//
//        // ★★★★★ 버튼 먹통 방지 안전장치 ★★★★★
//        // transitionend가 발생하지 않더라도, 애니메이션 시간 직후 isMoving을 풀어줌
//        setTimeout(() => {
//            isMoving = false;
//        }, transitionTime);
//
//
//        // 복제본 슬라이드 처리
//        if (current === slideCount || current === -1) {
//            setTimeout(() => {
//                slideWrap.style.transition = 'none';
//                current = (current === slideCount) ? 0 : slideCount - 1;
//                setSliderPosition();
//            }, transitionTime);
//        }
//    }
//
//    // 최초 실행 및 반응형 대응
//    // 이미지가 로드된 후 너비를 다시 계산하는 것이 가장 안정적입니다.
//    window.addEventListener('load', initialize);
//    window.addEventListener('resize', initialize);
//}
//
//document.addEventListener('DOMContentLoaded', setupSlider);