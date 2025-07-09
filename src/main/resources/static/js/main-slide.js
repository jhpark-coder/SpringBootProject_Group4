// main.js (모든 오류를 수정한 최종 완성 버전)
function setupSlider() {
    // 1. 필수 요소 선택
    const slideSection = document.querySelector('.slideSection');
    if (!slideSection) return;

    const slideWrap = slideSection.querySelector('.slideWrap');
    const originalSlides = slideSection.querySelectorAll('.slide');
    const nextBtn = slideSection.querySelector('.btn-next');
    const prevBtn = slideSection.querySelector('.btn-prev');
    const pagination = slideSection.querySelector('.pagination');

    if (!slideWrap || originalSlides.length === 0 || !nextBtn || !prevBtn || !pagination) {
        console.error('슬라이더 또는 페이지네이션에 필요한 요소가 HTML에 없습니다.');
        return;
    }

    // 2. 변수 및 상태 초기화
    const slideCount = originalSlides.length;
    let current = 0;
    let isMoving = false;
    const transitionTime = 500;

    // 3. 페이지네이션 생성
    for (let i = 0; i < slideCount; i++) {
        const pageDot = document.createElement('a');
        pageDot.dataset.index = i;
        pagination.appendChild(pageDot);
    }
    const pageDots = pagination.querySelectorAll('a');
    pageDots[0].classList.add('active');

    // 4. 복제본 생성 및 배치
    const firstClone = originalSlides[0].cloneNode(true);
    const lastClone = originalSlides[slideCount - 1].cloneNode(true);
    slideWrap.appendChild(firstClone);
    slideWrap.insertBefore(lastClone, slideWrap.firstElementChild);

    // 5. 핵심 함수 정의

    // 슬라이드 위치 계산 함수
    function setSliderPosition() {
        const containerWidth = slideSection.offsetWidth;
        const slideWidth = originalSlides[0].offsetWidth; // 이제 이 값은 초기에 정확해야 함
        const initialOffset = (containerWidth - slideWidth) / 2;
        const newLeft = initialOffset - slideWidth * (current + 1);
        slideWrap.style.left = `${newLeft}px`;
    }

    // 페이지네이션 활성화 업데이트 함수
    function updatePagination() {
        pageDots.forEach(dot => dot.classList.remove('active'));
        // 복제본 슬라이드에 있을 때도 진짜 슬라이드 인덱스에 맞게 표시
        const realIndex = (current + slideCount) % slideCount;
        pageDots[realIndex].classList.add('active');
    }

    // 슬라이드 이동 제어 함수
    function moveSlide(direction) {
        if (isMoving) return;
        isMoving = true;

        current += direction; // direction은 1(다음) 또는 -1(이전)
        slideWrap.style.transition = `${transitionTime}ms ease-out`;
        setSliderPosition();
        updatePagination();

        // 무한 반복 로직
        if (current === slideCount || current === -1) {
            setTimeout(() => {
                slideWrap.style.transition = 'none';
                // current를 실제 위치로 리셋
                current = (current === slideCount) ? 0 : slideCount - 1;
                setSliderPosition();
            }, transitionTime);
        }

        // 버튼 먹통 방지
        setTimeout(() => {
            isMoving = false;
        }, transitionTime);
    }

    // 특정 인덱스로 이동하는 함수
    function moveTo(index) {
        if (isMoving || current === index) return;
        isMoving = true;
        current = index;
        slideWrap.style.transition = `${transitionTime}ms ease-out`;
        setSliderPosition();
        updatePagination();
        setTimeout(() => { isMoving = false; }, transitionTime);
    }

    // 6. 이벤트 리스너 연결
    nextBtn.addEventListener('click', () => moveSlide(1));
    prevBtn.addEventListener('click', () => moveSlide(-1));

    pagination.addEventListener('click', (e) => {
        if (e.target.tagName === 'A') {
            e.preventDefault();
            const targetIndex = parseInt(e.target.dataset.index, 10);
            moveTo(targetIndex);
        }
    });

    // 7. 슬라이더 최초 실행 및 반응형 대응
    function initialize() {
        slideWrap.style.transition = 'none';
        setSliderPosition();
        // 다음 프레임에서 애니메이션을 다시 켜서 초기 로딩 깜빡임 방지
        requestAnimationFrame(() => {
            slideWrap.style.transition = `${transitionTime}ms ease-out`;
        });
    }

    window.addEventListener('load', initialize);
    window.addEventListener('resize', initialize);
}

// 스크립트 시작
document.addEventListener('DOMContentLoaded', setupSlider); 