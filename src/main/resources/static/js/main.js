(function () {
    'use strict';

    $(document).ready(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");

        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });

    // 🔒 JavaScript 필수 인증 + 메인 콘텐츠 로딩
    document.addEventListener('DOMContentLoaded', () => {

        // 🔒 1단계: JavaScript가 실행되었다는 증거로 경고 메시지 숨기기
        const noJsWarning = document.getElementById('no-js-warning');
        if (noJsWarning) {
            noJsWarning.style.display = 'none'; // JS 경고 숨김
        }

        // 🔒 2단계: 실제 콘텐츠 영역 보여주기
        const mainContainer = document.getElementById('main-content-container');
        if (!mainContainer) {
            console.error('Main content container not found!');
            return;
        }

        mainContainer.style.display = 'block'; // 실제 콘텐츠 영역 표시

        // 🔒 3단계: 이제 안전하게 데이터 로딩 시작
        mainContainer.innerHTML = '<h1>Nexus에 오신 것을 환영합니다!</h1><p>상품 목록을 불러오는 중입니다...</p>';

        // 🔒 4단계: API 호출해서 실제 데이터 가져오기 (CSRF 토큰 자동 포함됨)
        fetch('/api/products')
            .then(response => {
                if (!response.ok) throw new Error('데이터를 불러오는데 실패했습니다.');
                return response.json();
            })
            .then(products => {
                mainContainer.innerHTML = '<h1>Nexus에 오신 것을 환영합니다!</h1>';

                if (products.length === 0) {
                    mainContainer.innerHTML += '<p>표시할 상품이 없습니다.</p>';
                    return;
                }

                const productGrid = document.createElement('div');
                productGrid.className = 'product-grid';

                products.forEach(product => {
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

                mainContainer.appendChild(productGrid);
            })
            .catch(error => {
                console.error('Error fetching products:', error);
                mainContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
            });
    });

})(); 