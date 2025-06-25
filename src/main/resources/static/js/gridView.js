(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', () => {
        // 🔒 1단계: JavaScript가 실행되었다는 증거로 경고 메시지 숨기기
        const noJsWarning = document.getElementById('no-js-warning');
        if (noJsWarning) {
            noJsWarning.style.display = 'none'; // JS 경고 숨김
        }

        // 🔒 2단계: 실제 콘텐츠 영역 보여주기
        const gridContainer = document.getElementById('grid-container');
        if (!gridContainer) {
            console.error('Grid container not found!');
            return;
        }

        gridContainer.style.display = 'block'; // 실제 콘텐츠 영역 표시

        // 🔒 3단계: 이제 안전하게 그리드 생성 시작
        const productGrid = document.createElement('div');
        productGrid.className = 'product-grid';
        gridContainer.appendChild(productGrid);

        let currentPage = 0;
        const pageSize = 20; // 한 번에 불러올 상품 개수
        let isLoading = false;
        let isLastPage = false;

        // 🔒 4단계: 상품 데이터를 불러와 렌더링하는 함수 (CSRF 토큰 자동 포함됨)
        const fetchAndRenderProducts = () => {
            if (isLoading || isLastPage) return; // 로딩 중이거나 마지막 페이지면 중복 실행 방지

            isLoading = true;
            gridContainer.insertAdjacentHTML('beforeend', '<p class="loading-indicator">로딩 중...</p>');

            fetch(`/api/products?page=${currentPage}&size=${pageSize}&sort=id,asc`)
                .then(response => response.json())
                .then(pageData => {
                    // pageData.content 는 상품 목록 배열
                    // pageData.last 는 마지막 페이지인지 여부 (true/false)

                    // 로딩 인디케이터 제거
                    const loadingIndicator = gridContainer.querySelector('.loading-indicator');
                    if (loadingIndicator) {
                        loadingIndicator.remove();
                    }

                    if (pageData.content.length > 0) {
                        pageData.content.forEach(product => {
                            const card = document.createElement('div');
                            card.className = 'product-card';
                            card.innerHTML = `
                                <a href="/products/${product.id}">
                                    <img src="${product.imageUrl || '/images/placeholder.png'}" alt="${product.name}">
                                    <h3>${product.name}</h3>
                                    <p>${product.price.toLocaleString()}원</p>
                                </a>
                            `;
                            productGrid.appendChild(card);
                        });
                        currentPage++; // 다음 페이지를 위해 페이지 번호 증가
                    }

                    isLastPage = pageData.last;
                    isLoading = false;

                    if (isLastPage) {
                        gridContainer.insertAdjacentHTML('beforeend', '<p>모든 상품을 불러왔습니다.</p>');
                    }
                })
                .catch(error => {
                    console.error('Error fetching products:', error);
                    isLoading = false;
                });
        };

        // 스크롤 이벤트 리스너
        window.addEventListener('scroll', () => {
            // 사용자가 스크롤한 높이 + 보이는 창의 높이가 전체 문서 높이보다 크거나 같으면 (바닥에 닿으면)
            if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
                fetchAndRenderProducts();
            }
        });

        // 🔒 5단계: 첫 페이지 로드 (이제 안전하게 데이터 요청)
        fetchAndRenderProducts();
    });

})(); 