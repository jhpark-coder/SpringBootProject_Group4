document.addEventListener('DOMContentLoaded', function () {
    // 필요한 DOM 요소들 선택
    const sliderWrapper = document.querySelector('.slider-wrapper');
    const slides = document.querySelectorAll('.slide');
    const prevBtn = document.querySelector('.slider-arrow.prev');
    const nextBtn = document.querySelector('.slider-arrow.next');
    const paginationContainer = document.querySelector('.slider-pagination');

    let currentIndex = 0;
    const slideCount = slides.length;

    // 1. 페이지네이션 동적 생성
    for (let i = 0; i < slideCount; i++) {
        const dot = document.createElement('div');
        dot.classList.add('pagination-dot');
        dot.dataset.index = i; // 각 점에 인덱스 정보 저장
        paginationContainer.appendChild(dot);
    }

    const paginationDots = document.querySelectorAll('.pagination-dot');

    // 2. 특정 슬라이드로 이동하는 함수
    function goToSlide(index) {
        // 인덱스 범위 확인
        if (index < 0) {
            index = slideCount - 1;
        } else if (index >= slideCount) {
            index = 0;
        }

        currentIndex = index;

        // 슬라이더 래퍼를 translateX로 이동
        sliderWrapper.style.transform = `translateX(-${currentIndex * 100}%)`;

        // 페이지네이션 활성화 업데이트
        updatePagination();
    }

    // 3. 페이지네이션 활성화 상태 업데이트 함수
    function updatePagination() {
        paginationDots.forEach((dot, index) => {
            if (index === currentIndex) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
        });
    }

    // 4. 이벤트 리스너 설정
    // 다음 버튼 클릭 시
    nextBtn.addEventListener('click', () => {
        goToSlide(currentIndex + 1);
    });

    // 이전 버튼 클릭 시
    prevBtn.addEventListener('click', () => {
        goToSlide(currentIndex - 1);
    });

    // 페이지네이션 점 클릭 시
    paginationDots.forEach(dot => {
        dot.addEventListener('click', () => {
            // data-index에 저장된 인덱스 값을 숫자로 변환하여 사용
            const index = parseInt(dot.dataset.index);
            goToSlide(index);
        });
    });

    // 5. 초기 상태 설정
    goToSlide(0);
});