/**
 * Nexus Main Script
 *
 * 이 스크립트의 모든 코드는 jQuery의 $(document).ready() 내에서 실행됩니다.
 * 이는 페이지의 모든 DOM 요소와 jQuery 라이브러리가 완전히 로드되고 준비된 후에만
 * 코드가 실행되도록 보장하여, '$' is not defined 오류를 원천적으로 방지합니다.
 */
$(document).ready(function () {
    'use strict';

    // CSRF 토큰을 모든 AJAX 요청에 자동으로 추가
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    if (token && header) {
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    }

    // 콘텐츠를 삽입할 위치와 경고 메시지 요소
    const contentPlaceholder = $('#main-content-placeholder');

    if (contentPlaceholder.length === 0) {
        console.error('필수 DOM 요소(placeholder)를 찾을 수 없습니다.');
        return;
    }

    // 1. 서버에서 메인 콘텐츠의 HTML 구조를 가져옴
    $.get("/main-content", function (html) {
        // 2. 가져온 HTML을 placeholder에 삽입
        contentPlaceholder.html(html);

        // 3. 동적으로 삽입된 콘텐츠 내부의 상품 그리드에 상품 목록을 채움
        loadProducts();
    }).fail(function (error) {
        console.error('메인 콘텐츠를 가져오는 중 오류 발생:', error);
        contentPlaceholder.html('<p style="color: red; text-align: center;">페이지를 로드할 수 없습니다. 잠시 후 다시 시도해주세요.</p>');
    });

    /**
     * 상품 목록을 API에서 가져와 그리드에 렌더링하는 함수
     */
    function loadProducts() {
        const gridContainer = $('.grid-container');
        if (gridContainer.length === 0) {
            console.error('상품 그리드 컨테이너를 찾을 수 없습니다.');
            return;
        }

        gridContainer.html('<p>상품 목록을 불러오는 중입니다...</p>');

        $.getJSON("/api/products")
            .done(function (pageResponse) {
                const products = pageResponse.content;
                gridContainer.empty(); // 로딩 메시지 제거

                if (!products || products.length === 0) {
                    gridContainer.html('<p>표시할 상품이 없습니다.</p>');
                    return;
                }

                products.forEach(function (product) {
                    const productItem = `
                        <article class="grid-item">
                            <div class="item-image-placeholder">
                               <a href="/products/${product.id}">
                                 <img src="${product.imageUrl || '/images/placeholder.png'}" alt="${product.name}" style="width: 100%; height: 100%; object-fit: cover;">
                               </a>
                            </div>
                            <div class="item-info">
                                <div class="author-info">
                                    <div class="author-avatar"></div>
                                    <span class="author-name">${product.name}</span>
                                </div>
                                <a href="#" class="btn subscribe-btn">Subscribe</a>
                            </div>
                        </article>`;
                    gridContainer.append(productItem);
                });
            })
            .fail(function (error) {
                console.error('상품 데이터를 가져오는 중 오류 발생:', error);
                gridContainer.html(`<p style="color: red;">데이터를 불러오는데 실패했습니다.</p>`);
            });
    }
}); 