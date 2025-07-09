// grid-animation.js

class GridAnimation {
    constructor(el, row = 9, col = 9) {
        this.element = el;
        const rect = this.element.getBoundingClientRect();

        // 박스와 조각의 크기를 계산하여 CSS 변수로 저장 (1회 실행)
        this.element.style.setProperty('--box-width', `${rect.width}px`);
        this.element.style.setProperty('--box-height', `${rect.height}px`);
        this.element.style.setProperty('--frag-width', `${rect.width / col}px`);
        this.element.style.setProperty('--frag-height', `${rect.height / row}px`);

        // 클래스 내부 속성 설정
        this.row = row;
        this.col = col;
        this.duration = 1500;
        this.delayDelta = 70;
        this.type = null;
        this.isAnimated = false;
        this.randomIntBetween = (min, max) => Math.floor(Math.random() * (max - min + 1) + min);
    }

    trigger = () => {
        if (this.isAnimated) return;

        while (this.element.hasChildNodes()) {
            this.element.removeChild(this.element.firstChild);
        }
        this.element.classList.add('hide');
        this.animate();
        this.isAnimated = true;
    }

    setType = (type) => {
        this.type = type;
    }

    animate = () => {
        if (this.type === null) return;

        const fragWidth = parseFloat(this.element.style.getPropertyValue('--frag-width'));
        const fragHeight = parseFloat(this.element.style.getPropertyValue('--frag-height'));

        for (let i = 0; i < this.row; i++) {
            for (let j = 0; j < this.col; j++) {
                const fragment = document.createElement('div');
                fragment.className = 'fragment';

                // --- JS의 유일한 임무: CSS 변수 설정 ---
                const translateX = j * fragWidth;
                const translateY = i * fragHeight;
                fragment.style.setProperty('--translate-x', `${translateX}px`);
                fragment.style.setProperty('--translate-y', `${translateY}px`);

                fragment.style.setProperty('--x-offset', `${-translateX}px`);
                fragment.style.setProperty('--y-offset', `${-translateY}px`);

                let delay = 0;
                // ... (delay 계산 switch문은 그대로 유지) ...
                switch (this.type) {
                    case 0: delay = i * 2; break;
                    case 1: delay = j * 2; break;
                    case 2: delay = this.randomIntBetween(0, (this.col - 1) + (this.row - 1)); break;
                    case 3: delay = ((this.col - 1) + (this.row - 1)) - (j + i); break;
                    default: delay = i + j;
                }

                const isOdd = (i + j) % 2 === 0;
                fragment.style.setProperty('--rotateX', `rotateX(${isOdd ? -180 : 0}deg)`);
                fragment.style.setProperty('--rotateY', `rotateY(${isOdd ? 0 : -180}deg)`);
                fragment.style.setProperty('--delay', (delay * this.delayDelta) + 'ms');
                fragment.style.setProperty('--duration', this.duration + 'ms');

                this.element.appendChild(fragment);
            }
        }
    }
}

// --- Intersection Observer 실행 코드 ---
const initGridAnimations = () => {
    const boxes = document.querySelectorAll('.box:not(.grid-animation-initialized)');

    if (boxes.length === 0) return;

    const observerCallback = (entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const box = entry.target;
                if (box.gridAnimation) {
                    box.gridAnimation.trigger();
                }
                observer.unobserve(box);
            }
        });
    };
    const observerOptions = { root: null, rootMargin: '0px', threshold: 0.3 };
    const observer = new IntersectionObserver(observerCallback, observerOptions);

    boxes.forEach((box, index) => {
        // --- 여기가 핵심! ---
        // 1. 각 박스에 고유한 이미지 URL을 CSS 변수로 설정합니다.
        const imageUrl = box.getAttribute('data-img-url');
        if (imageUrl) {
            box.style.setProperty('--img-url', `url('${imageUrl}')`);
        }

        // 2. 애니메이션 객체 생성
        const gridAnimation = new GridAnimation(box);
        const type = box.hasAttribute('data-i') ? parseInt(box.getAttribute('data-i')) : (index % 14);
        gridAnimation.setType(type);

        // 3. 객체 저장 및 초기화 완료 표시
        box.gridAnimation = gridAnimation;
        box.classList.add('grid-animation-initialized'); // 중복 실행 방지
        observer.observe(box);
    });
};

document.addEventListener('DOMContentLoaded', initGridAnimations);
// 무한 스크롤 후에는 initGridAnimations()를 호출해주세요. 