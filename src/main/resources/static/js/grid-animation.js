class GridAnimation {
      constructor(el, row = 6, col = 6) {
        this.element = el
        const rect = this.element.getBoundingClientRect();
          // CSS 변수에 실제 계산된 너비와 높이를 설정합니다.
        this.element.style.setProperty('--box-width', `${rect.width}px`);
        this.element.style.setProperty('--box-height', `${rect.height}px`);

        // 💡 조각 하나의 크기를 미리 계산해서 CSS 변수로 설정
        const fragWidth = rect.width / col;
        const fragHeight = rect.height / row;
        this.element.style.setProperty('--frag-width', `${fragWidth}px`);
        this.element.style.setProperty('--frag-height', `${fragHeight}px`);


        this.fragments = el.children
        this.row = row
        this.col = col
        this.duration = 1500
        this.delayDelta = 50
        this.type = null
        this.isAnimated = false // 자동 실행을 위해 실행여부 변수 생성

        this.randomIntBetween = (min, max) => {
          return Math.floor(Math.random() * (max - min + 1) + min)
        }

        this.element.style.setProperty('--row', this.row)
        this.element.style.setProperty('--col', this.col)
//        this.element.addEventListener('click', this.trigger) 자동 실행을 위해 클릭 트리거 무효화
      }

      trigger = () => {
        if(this.isAnimated) return;

        if (this.fragments.length > 0) this.clear()
        this.element.classList.add('hide')
        this.animate()
        this.isAnimated = true; // 애니메이션 실행 후 실행여부 true 반환
      }

      setType = (type) => {
        this.type = type
      }

      clear = () => {
        while (this.element.hasChildNodes()) {
          this.element.removeChild(this.element.firstChild)
        }
      }

      animate = () => {
        if (this.type === null) return
        const x = this.col - 1
        const y = this.row - 1
        for (let i = 0; i < this.row; i++) {
          for (let j = 0; j < this.col; j++) {
            const fragment = document.createElement('div')
            fragment.className = 'fragment'
            fragment.style.setProperty('--x', j)
            fragment.style.setProperty('--y', i)

            let delay = 0
            switch (this.type) {
              case  0: delay = i * 2; break
              case  1: delay = j * 2; break
              case  2: delay = this.randomIntBetween(0, x + y); break
              case  3: delay = (x + y) - (j + i); break
              case  4: delay = i + j; break
              case  5: delay = (x - i) + j; break
              case  6: delay = i + (y - j); break
              case  7: delay = Math.abs((x + y) / 2 - (j + i)); break
              case  8: delay = (x + y) / 2 - Math.abs((x + y) / 2 - (j + i)); break
              case  9: delay = (x + y) / 2 - Math.abs((x + y) / 2 - (j + i)) * Math.cos(i + j); break
              case 10: delay = Math.abs((x + y) / 2 - ((x - j) + i)); break
              case 11: delay = Math.abs((x + y) / 2 - Math.abs((x + y) / 2 - ((x - j) + i))); break
              case 12: delay = Math.abs((x / 2) - j) + Math.abs((y / 2) - i); break
              case 13: delay = x / 2 - Math.abs((x / 2) - j) + (x / 2 - Math.abs((y / 2) - i)); break
            }

            const isOdd = (i + j) % 2 === 0
            fragment.style.setProperty('--rotateX', `rotateX(${isOdd ? -180 : 0}deg)`)
            fragment.style.setProperty('--rotateY', `rotateY(${isOdd ? 0 : -180}deg)`)
            fragment.style.setProperty('--delay', delay * this.delayDelta + 'ms')
            fragment.style.setProperty('--duration', this.duration + 'ms')
            this.element.appendChild(fragment)

            const timer = setTimeout(() => {
              fragment.style.willChange = 'initial'
              fragment.style.transform = 'initial'
              fragment.style.animation = 'initial'
              fragment.style.backfaceVisibility = 'initial'
              clearTimeout(timer)
            }, this.duration + delay * this.delayDelta)
          }
        }
      }
    }
//    클릭 트리거를 활용한 Js 실행 코드
//    document.querySelectorAll('.box').forEach((box, index) => {
//      const gridAnimation = new GridAnimation(box)
//      const type = parseInt(box.getAttribute('data-i'))
//      gridAnimation.setType(type)
//      if (index === 0) gridAnimation.trigger()
//    });

    // Intersection Observer를 사용한 실행 코드
    const boxes = document.querySelectorAll('.box');

    // Intersection Observer 콜백 함수: 감시 대상이 화면에 나타나거나 사라질 때 실행됨
    const observerCallback = (entries, observer) => {
      entries.forEach(entry => {
        // entry.isIntersecting: 감시 대상이 화면과 교차(보임)하는지 여부를 true/false로 알려줌
        if (entry.isIntersecting) {
          const box = entry.target; // 화면에 나타난 바로 그 박스 요소
          // box.gridAnimation은 아래에서 우리가 직접 추가해준 속성입니다.
          if (box.gridAnimation) {
            box.gridAnimation.trigger(); // 해당 박스의 애니메이션을 발동!
          }

          // 한 번 애니메이션을 실행한 요소는 더 이상 감시하지 않음 (성능 최적화)
          observer.unobserve(box);
        }
      });
    };

    // Intersection Observer 옵션
    const observerOptions = {
      root: null, // null이면 브라우저의 화면(viewport)을 기준으로 함
      rootMargin: '0px',
      threshold: 0.5 // 감시 대상이 30% 이상 보였을 때 콜백 함수를 실행
    };

    // Intersection Observer 인스턴스 생성
    const observer = new IntersectionObserver(observerCallback, observerOptions);

    // 모든 .box 요소들에 대해 GridAnimation을 준비하고, Observer의 감시 목록에 추가
    boxes.forEach(box => {
      // 1. 각 박스에 대한 GridAnimation 인스턴스(객체)를 생성합니다.
      const gridAnimation = new GridAnimation(box);
      const type = parseInt(box.getAttribute('data-i')) || 4; // data-i가 없으면 기본값 0
      gridAnimation.setType(type);

        // --- 💡 여기가 핵심 수정 부분! ---
        // 1. HTML의 data-img-url 속성에서 이미지 URL을 가져옵니다.
        const imageUrl = box.getAttribute('data-img-url');

        // 2. 이미지 URL이 존재한다면, 해당 box의 인라인 스타일로 --img-url 변수를 설정합니다.
        if (imageUrl) {
          box.style.setProperty('--img-url', `url('${imageUrl}')`);
        }
        // ------------------------------------


      // 2. 나중에 콜백 함수에서 쓸 수 있도록, 만든 인스턴스를 박스 요소 자체에 저장합니다.
      box.gridAnimation = gridAnimation;

      // 3. "경비원(observer)님, 이 박스(box) 좀 감시해주세요!" 라고 등록합니다.
      observer.observe(box);
    });