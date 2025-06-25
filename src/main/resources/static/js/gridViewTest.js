/**
 * 이 파일은 무한 스크롤 기능을 구현한 스크립트입니다.
 * 즉시 실행 함수(IIFE)로 전체 코드를 감싸, 전역 스코프의 오염을 방지합니다.
 */
(function () {
    'use strict';

    // --- [즉시 실행] UI 초기화 ---
    // JavaScript가 활성화되었음을 즉시 사용자에게 보여줍니다.
    // 'no-js-warning' 메시지를 숨기고, 실제 콘텐츠를 담을 'grid-container'를 보여줍니다.
    const noJsWarning = document.getElementById('no-js-warning');
    if (noJsWarning) {
        noJsWarning.style.display = 'none';
    }

    const gridContainer = document.getElementById('grid-container');
    if (gridContainer) {
        gridContainer.style.display = 'block'; // 컨테이너를 보이게 처리
    } else {
        // 컨테이너가 없으면 이후 작업이 불가능하므로 중단합니다.
        console.error('Grid container not found!');
        return;
    }

    /**
     * 데이터 로딩 및 렌더링을 수행하는 메인 로직 함수.
     */
    const initializeGrid = () => {
        // --- [순서 1] 변수 및 요소 준비 ---

        // 실제 상품 카드들이 담길 'product-grid' div를 동적으로 생성하고 컨테이너에 추가합니다.
        const productGrid = document.createElement('div');
        productGrid.className = 'product-grid';
        gridContainer.appendChild(productGrid);


        // --- [순서 2] 무한 스크롤 상태 관리를 위한 변수 선언 ---

        let currentPage = 0;      // API에 요청할 현재 페이지 번호 (0부터 시작).
        const pageSize = 20;      // 한 페이지에 불러올 상품의 개수 (상수).
        let isLoading = false;    // 데이터 로딩 중 중복 요청을 방지하기 위한 플래그.
        let isLastPage = false;   // 마지막 페이지까지 모두 로드했는지 확인하는 플래그.


        // --- [순서 3] 핵심 기능 메소드 정의 ---

        /**
         * 서버로부터 상품 데이터를 비동기적으로 불러와 화면에 렌더링하는 함수.
         */
        const fetchAndRenderProducts = () => {
            // (A) 방어 코드: 이미 로딩 중이거나, 마지막 페이지라면 함수를 즉시 종료합니다.
            if (isLoading || isLastPage) return;

            // (B) 로딩 시작: 상태를 '로딩 중'으로 변경하고, 사용자에게 '로딩 중...' 메시지를 보여줍니다.
            isLoading = true;
            gridContainer.insertAdjacentHTML('beforeend', '<p class="loading-indicator">로딩 중...</p>');

            // (C) 데이터 요청: fetch API를 사용하여 서버에 상품 목록을 요청합니다.
            // security.js의 CSRF 기능 덕분에 이 요청에는 자동으로 토큰이 포함됩니다.
            fetch(`/api/products?page=${currentPage}&size=${pageSize}&sort=id,asc`)
                .then(response => response.json()) // 응답을 JSON 형태로 파싱합니다.
                .then(pageData => { // 파싱된 데이터(pageData)를 사용하여 다음 작업을 수행합니다.

                    // (D) 로딩 완료 처리: '로딩 중...' 메시지를 화면에서 제거합니다.
                    const loadingIndicator = gridContainer.querySelector('.loading-indicator');
                    if (loadingIndicator) {
                        loadingIndicator.remove();
                    }

                    // (E) 화면 렌더링: 받아온 데이터(pageData.content)가 있을 경우, 각 상품을 화면에 추가합니다.
                    if (pageData.content && pageData.content.length > 0) {
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
                            productGrid.appendChild(card); // 생성된 카드를 그리드에 추가
                        });
                        // (F) 상태 업데이트: 다음 요청을 위해 페이지 번호를 1 증가시킵니다.
                        currentPage++;
                    }

                    // (G) 마지막 페이지 확인: 서버가 보내준 'last' 플래그 값으로 상태를 업데이트합니다.
                    isLastPage = pageData.last;
                    // (H) 로딩 상태 해제: 로딩이 끝났음을 표시합니다.
                    isLoading = false;

                    // 만약 마지막 페이지였다면, 사용자에게 알려주는 메시지를 추가합니다.
                    if (isLastPage) {
                        gridContainer.insertAdjacentHTML('beforeend', '<p>모든 상품을 불러왔습니다.</p>');
                    }
                })
                .catch(error => { // 요청 실패 시 에러 처리
                    console.error('Error fetching products:', error);
                    const loadingIndicator = gridContainer.querySelector('.loading-indicator');
                    if (loadingIndicator) {
                        loadingIndicator.remove();
                    }
                    gridContainer.insertAdjacentHTML('beforeend', '<p class="error-message">데이터를 불러오는 데 실패했습니다.</p>');
                    isLoading = false; // 에러가 발생해도 로딩 상태는 해제해야 합니다.
                });
        };


        // --- [순서 4] 이벤트 리스너 등록 ---

        /**
         * 스크롤 이벤트 리스너: 사용자가 스크롤을 할 때마다 실행됩니다.
         */
        window.addEventListener('scroll', () => {
            // 현재 화면의 맨 아래가, 문서 전체 높이의 끝에서 100px 안쪽으로 들어왔는지 확인합니다.
            if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
                // 조건이 참이면, 다음 페이지를 로드하기 위해 핵심 메소드를 호출합니다.
                fetchAndRenderProducts();
            }
        });


        // --- [순서 5] 최초 데이터 로드 ---

        // 페이지에 처음 진입했을 때, 첫 페이지의 상품들을 불러오기 위해 함수를 즉시 호출합니다.
        fetchAndRenderProducts();
    };

    // --- [최종 실행] ---
    // security.js가 이미 실행되어 '깃발'을 세웠는지 확인합니다.
    if (window.securityIsInitialized) {
        // 이미 완료되었다면, 즉시 메인 로직을 실행합니다.
        initializeGrid();
    } else {
        // 아직 완료되지 않았다면, 'security-initialized' 이벤트가 발생할 때까지 기다립니다.
        document.addEventListener('security-initialized', initializeGrid);
    }

})(); 