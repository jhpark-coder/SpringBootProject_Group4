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
                    // 로딩 중이거나, 더 이상 데이터가 없거나, 총 페이지 수를 초과하면 중단
                    if (isLoading || noMoreData || currentPage >= totalPages) {
                        return;
                    }
                    isLoading = true;
                    $('#loading-spinner').show();

                    // API 엔드포인트 경로 수정 및 페이지 번호 직접 사용
                    const url = `/api/products/category?primary=${primaryCategory}&secondary=${secondaryCategory}&page=${currentPage}&size=16&sort=regTime,desc`;

                    $.get(url, function (response) {
                        const products = response.products;
                        if (products && products.length > 0) {
                            products.forEach(product => {
                                $('#product-grid').append(createProductCard(product));
                            });
                            currentPage++; // 다음 페이지를 불러오기 위해 페이지 번호 증가
                        }

                        // 응답의 last 플래그를 사용하여 더 이상 데이터가 없는지 확인
                        if (response.last) {
                            noMoreData = true;
                            $('#loading-spinner').hide(); // 더 이상 데이터가 없으면 로딩 스피너 숨김
                        }

                        isLoading = false;
                        // hide는 성공/실패 여부와 관계없이 항상 수행되도록 finally 블록처럼 처리
                    }).fail(function (xhr, status, error) {
                        console.error("상품 로딩 중 오류 발생:", status, error);
                        noMoreData = true; // 에러 발생 시 더 이상 시도하지 않음
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

                // 강제 리플로우
                setTimeout(() => {
                    document.body.style.display = 'none';
                    void document.body.offsetHeight;
                    document.body.style.display = '';
                }, 0);
            });
            /*]]>*/