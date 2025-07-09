/**
 * Nexus Main Script (Simplified)
 * 동적 상품 목록을 로드하고, 완료 시 'pageReady' 이벤트를 발생시키는 역할에 집중합니다.
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
            // 메인 페이지가 아니므로 동적 컨텐츠 로딩이 없음.
            // 즉시 'pageReady' 이벤트를 발생시켜 다른 페이지에서도 챗봇이 뜨게 함.
            console.log("Main grid not found. Dispatching 'pageReady' immediately.");
            document.dispatchEvent(new CustomEvent('pageReady'));
            return;
        }

        gridContainer.html('<p>추천 상품을 불러오는 중입니다...</p>');

        // 팀원이 만든 개인화 추천 API 사용
        $.getJSON("/api/main/recommendations")
            .done(function (products) {
                gridContainer.empty();

                if (!products || products.length === 0) {
                    gridContainer.html('<p>표시할 상품이 없습니다.</p>');
                    return; // 상품 렌더링 없이 done 콜백 종료
                }

                products.forEach(function (product) {
                    // sellerId가 있는 경우에만 팔로우 버튼 생성
                    const followButton = product.sellerId ?
                        `<button class="btn follow-btn"
                                 data-member-id="${product.sellerId}"
                                 onclick="toggleFollow(${product.sellerId})">Follow</button>` : '';

                    const productItem = `
                        <div class="col-md-3">
                            <article class="grid-item">
                                <a href="/products/${product.id}">
                                    <div class="item-image-placeholder"
                                        style="background-image: url('${product.imageUrl || '/images/placeholder.png'}'); background-size: cover; background-position: center; width: 100%; height: 200px; border-radius: 8px;"></div>
                                </a>
                                <div class="item-info">
                                    <div class="seller-info">
                                        <div class="seller-avatar"></div>
                                        <span class="seller-name">${product.sellerName || '판매자 이름'}</span>
                                    </div>
                                    ${followButton}
                                </div>
                            </article>
                        </div>
                    `;
                    gridContainer.append(productItem);
                });
            })
            .fail(function (error) {
                console.error('추천 상품 데이터를 가져오는 중 오류 발생:', error);
                gridContainer.html(`<p style="color: red;">추천 데이터를 불러오는데 실패했습니다.</p>`);
            })
            .always(function () {
                // ★★★★★ 여기가 핵심 ★★★★★
                // API 호출이 성공하든, 실패하든, 모든 작업이 끝나면 이벤트를 발생시킵니다.
                console.log("Main content loading process finished. Dispatching 'pageReady' event.");
                document.dispatchEvent(new CustomEvent('pageReady'));
            });
    }

    // 페이지 로드 시 바로 실행
    loadProducts();

    // toggleFollow와 같은 다른 전역 함수가 있었다면 여기에 유지합니다.
    // window.toggleFollow = function(sellerId) { ... };
});