/*<![CDATA[*/
$(document).ready(function () {
    const primaryCategory = /*[[${primaryCategory}]]*/ 'artwork';
    const secondaryCategory = /*[[${secondaryCategory}]]*/ 'all';
    const totalPages = /*[[${totalPages}]]*/ 1;

    // 서버가 렌더링한 페이지는 0페이지(UI에서는 1페이지). JS는 1페이지부터 요청 시작.
    let currentPage = 1;
    let isLoading = false;
    // 서버에서 전달된 첫 페이지가 마지막 페이지라면, 더이상 로드할 필요 없음
    let noMoreData = /*[[${initialProductPage.last}]]*/ false;

    function createProductCard(product) {
        // sellerId가 있는 경우에만 팔로우 버튼 생성
        const followButton = product.sellerId ?
            `<button class="btn follow-btn"
                     data-member-id="${product.sellerId}"
                     onclick="toggleFollow(${product.sellerId})">Follow</button>` : '';

        return `
            <div class="col-md-3">
                <article class="grid-item">
                    <a href="/products/${product.id}">
                        <div class="item-image-placeholder" style="background-image: url('${product.imageUrl}');"></div>
                    </a>
                    <div class="item-info">
                        <div class="seller-info">
                            <div class="seller-avatar"></div>
                            <span class="seller-name">${product.sellerName}</span>
                        </div>
                        ${followButton}
                    </div>
                </article>
            </div>
        `;
    }

    function loadProducts() {
        if (isLoading || noMoreData || currentPage >= totalPages) {
            return;
        }
        isLoading = true;
        $('#loading-spinner').show();

        const url = `/api/products/category?primary=${primaryCategory}&secondary=${secondaryCategory}&page=${currentPage}&size=16&sort=regTime,desc`;

        $.get(url, function (response) {
            const products = response.products;
            if (products && products.length > 0) {
                products.forEach(product => {
                    $('#product-grid').append(createProductCard(product));
                });
                currentPage++;
                // 새로 추가된 상품들에 대해 애니메이션 초기화 (지연 실행)
                if (typeof initGridAnimations === 'function') {
                    setTimeout(() => {
                        initGridAnimations();

                        // 동적 데이터 로딩 후 뷰포트 갱신 및 버튼 위치 재설정
                        setTimeout(() => {
                            const chatbotBtn = document.querySelector('.chatbot-btn');
                            const chatBtn = document.querySelector('.chat-btn');

                            if (chatbotBtn) {
                                chatbotBtn.style.setProperty('right', '40px', 'important');
                                chatbotBtn.style.setProperty('bottom', '120px', 'important');
                            }
                            if (chatBtn) {
                                chatBtn.style.setProperty('right', '40px', 'important');
                                chatBtn.style.setProperty('bottom', '40px', 'important');
                            }

                            // 뷰포트 강제 갱신 (스크롤 위치 보존)
                            const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop;
                            const currentScrollLeft = window.pageXOffset || document.documentElement.scrollLeft;

                            // 강제 리플로우 없이 뷰포트 갱신
                            window.dispatchEvent(new Event('resize'));

                            // 스크롤 위치 복원
                            window.scrollTo(currentScrollLeft, currentScrollTop);
                        }, 500);
                    }, 200);
                }
            }
            if (response.last) {
                noMoreData = true;
                $('#loading-spinner').hide();
            }
            isLoading = false;
        }).fail(function (xhr, status, error) {
            console.error("상품 로딩 중 오류 발생:", status, error);
            noMoreData = true;
        }).always(function () {
            $('#loading-spinner').hide();
            isLoading = false;
        });
    }

    $(window).scroll(function () {
        // 스크롤이 하단에 가까워졌을 때 다음 페이지 로드
        if ($(window).scrollTop() + $(window).height() > $(document).height() - 300) {
            loadProducts();
        }
    });
});
/*]]>*/ 