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

    // 상품 목록을 API에서 가져와 그리드에 렌더링하는 함수
    function loadProducts() {
        const gridContainer = $('#main-product-grid');
        if (gridContainer.length === 0) {
            console.error('상품 그리드 컨테이너를 찾을 수 없습니다.');
            return;
        }

        gridContainer.html('<p>상품 목록을 불러오는 중입니다...</p>');

        $.getJSON("/api/main/recommendations")
            .done(function (products) { // 서버 응답이 이 'products' 변수에 담겨 들어옵니다.
                // [삭제 및 수정] 'pageResponse'를 사용하던 잘못된 라인을 삭제하고,
                // 파라미터로 받은 'products'를 직접 사용합니다.
                gridContainer.empty(); // 그리드를 비웁니다.

                if (!products || products.length === 0) {
                    gridContainer.html('<p>표시할 상품이 없습니다.</p>');
                    return;
                }

                // 응답 데이터(products)를 반복하여 그리드 아이템을 생성합니다.
                products.forEach(function (product) {
                    const productItem = `
                        <div class="col-md-3">
                            <article class="grid-item">
                                <a href="/products/${product.id}">
                                    <div class="item-image-placeholder"
                                         style="background-image: url('${product.imageUrl || '/images/placeholder.png'}'); background-size: cover; background-position: center; width: 100%; height: 200px; border-radius: 8px;"></div>
                                </a>
                                <div class="item-info">
                                    <div class="author-info">
                                        <div class="author-avatar"></div>
                                        <span class="author-name">${product.authorName || '작가 이름'}</span>
                                    </div>
                                    <a href="#" class="btn subscribe-btn">Subscribe</a>
                                </div>
                            </article>
                        </div>
                    `;
                    gridContainer.append(productItem);
                });
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.error('추천 상품 데이터를 가져오는 중 오류 발생:', textStatus, errorThrown);
                gridContainer.html(`<p style="color: red;">데이터를 불러오는데 실패했습니다. 서버 로그를 확인해주세요.</p>`);
            });
    }

    // 페이지 로드 시 상품 목록을 바로 로드합니다.
    loadProducts();
});

///**
// * Nexus Main Script
// *
// * 이 스크립트의 모든 코드는 jQuery의 $(document).ready() 내에서 실행됩니다.
// * 이는 페이지의 모든 DOM 요소와 jQuery 라이브러리가 완전히 로드되고 준비된 후에만
// * 코드가 실행되도록 보장하여, '$' is not defined 오류를 원천적으로 방지합니다.
// */
//$(document).ready(function () {
//    'use strict';
//
//    // CSRF 토큰을 모든 AJAX 요청에 자동으로 추가
//    const token = $("meta[name='_csrf']").attr("content");
//    const header = $("meta[name='_csrf_header']").attr("content");
//    if (token && header) {
//        $(document).ajaxSend(function (e, xhr, options) {
//            xhr.setRequestHeader(header, token);
//        });
//    }
//
//    // 상품 목록을 API에서 가져와 그리드에 렌더링하는 함수
//    function loadProducts() {
//        const gridContainer = $('#main-product-grid');
//        if (gridContainer.length === 0) {
//            console.error('상품 그리드 컨테이너를 찾을 수 없습니다.');
//            return;
//        }
//
//        gridContainer.html('<p>상품 목록을 불러오는 중입니다...</p>');
//
//        $.getJSON("/api/main/recommendations")
//            .done(function (products) {
//                const products = pageResponse.content || pageResponse.products || pageResponse || [];
//                gridContainer.empty();
//
//                if (!products || products.length === 0) {
//                    gridContainer.html('<p>표시할 상품이 없습니다.</p>');
//                    return;
//                }
//
//                // 응답 데이터(products)를 바로 사용합니다.
//                    products.forEach(function (product) {
//                        const productItem = `
//                            <div class="col-md-3">
//                                <article class="grid-item">
//                                    <a href="/products/${product.id}">
//                                        <div class="item-image-placeholder"
//                                            style="background-image: url('${product.imageUrl || '/images/placeholder.png'}'); background-size: cover; background-position: center; width: 100%; height: 200px; border-radius: 8px;"></div>
//                                    </a>
//                                    <div class="item-info">
//                                        <div class="author-info">
//                                            <div class="author-avatar"></div>
//                                            <span class="author-name">${product.authorName || '작가 이름'}</span>
//                                        </div>
//                                        <a href="#" class="btn subscribe-btn">Subscribe</a>
//                                    </div>
//                                </article>
//                            </div>
//                        `;
//                        gridContainer.append(productItem);
//                    });
//                })
//                .fail(function (error) {
//                    console.error('추천 상품 데이터를 가져오는 중 오류 발생:', error);
//                    gridContainer.html(`<p style="color: red;">데이터를 불러오는데 실패했습니다.</p>`);
//                });
//        }
//
//        // 페이지 로드 시 바로 실행
//        loadProducts();
//    });