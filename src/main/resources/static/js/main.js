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

        gridContainer.html('<p>추천 상품을 불러오는 중입니다...</p>');

        // 팀원이 만든 개인화 추천 API 사용
        $.getJSON("/api/main/recommendations")
            .done(function (products) {
                gridContainer.empty();

                if (!products || products.length === 0) {
                    gridContainer.html('<p>표시할 상품이 없습니다.</p>');
                    return;
                }

                products.forEach(function (product) {
                    // sellerId가 있는 경우에만 팔로우 버튼 생성
                    let followBtnClass = 'btn follow-btn';
                    let followBtnText = 'Follow';
                    if (product.isFollowing) {
                        followBtnClass += ' following';
                        followBtnText = 'Following';
                    }
                    const followButton = product.sellerId ?
                        `<button class="${followBtnClass}"
                                 data-member-id="${product.sellerId}"
                                 onclick="toggleFollow(${product.sellerId}, this)">${followBtnText}</button>` : '';

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
                // 강제 리플로우를 requestAnimationFrame으로 개선
                requestAnimationFrame(() => {
                    if (gridContainer[0]) {
                        gridContainer[0].style.opacity = '0.99';
                        void gridContainer[0].offsetHeight;
                    }
                    requestAnimationFrame(() => {
                        if (gridContainer[0]) {
                            gridContainer[0].style.opacity = '';
                        }
                    });
                });
            })
            .fail(function (error) {
                console.error('추천 상품 데이터를 가져오는 중 오류 발생:', error);
                gridContainer.html(`<p style="color: red;">추천 데이터를 불러오는데 실패했습니다.</p>`);
            });
    }

    // 페이지 로드 시 바로 실행
    loadProducts();
}); 