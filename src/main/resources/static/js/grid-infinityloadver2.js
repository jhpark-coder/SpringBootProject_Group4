/*<![CDATA[*/
$(document).ready(function () {
    // 서버로부터 전달받는 값들
    const primaryCategory = /*[[${primaryCategory}]]*/ 'default';
    const secondaryCategory = /*[[${secondaryCategory}]]*/ 'all';
    const totalPages = /*[[${totalPages}]]*/ 1;

    // 무한 스크롤 상태 관리 변수
    let currentPage = 1; // 서버는 0-based index, JS는 1부터 다음 페이지 요청
    let isLoading = false;
    let noMoreData = /*[[${initialProductPage.last}]]*/ false;

    /**
     * 애니메이션이 적용될 수 있는 올바른 HTML 구조로 상품 카드를 생성합니다.
     * @param {object} product - 상품 정보 객체
     * @param {number} index - 현재 로드된 목록에서의 인덱스
     * @returns {string} - 생성된 HTML 문자열
     */
    function createProductCard(product, index) {
        // Thymeleaf가 처음 렌더링하는 구조와 동일하게 맞춥니다.

        // 1. Follow 버튼 HTML 생성
        const followButtonHtml = product.sellerId ?
            `<button class="btn follow-btn"
                     th:if="${product.sellerId != null}"
                     data-member-id="${product.sellerId}"
                     onclick="toggleFollow(${product.sellerId})"
                     sec:authorize="isAuthenticated()">Follow</button>
             <button class="btn follow-btn"
                     th:if="${product.sellerId != null}"
                     onclick="alert('로그인이 필요합니다.'); location.href='/members/login';"
                     sec:authorize="!isAuthenticated()">Follow</button>` : '';

        // 2. data-i 값을 동적으로 계산하여 다양한 애니메이션 효과 적용
        const existingItemCount = $('#product-grid .grid-item').length;
        const dataI = (existingItemCount + index) % 14;

        // 3. 최종 HTML 구조 반환
        return `
            <div class="col-md-3">
                <article class="grid-item">
                    <!-- 애니메이션 대상: .box 클래스와 data-* 속성 포함 -->
                    <div class="item-image-placeholder box"
                         data-img-url="${product.imageUrl}"
                         data-i="${dataI}">
                    </div>

                    <!-- 상품 정보 -->
                    <div class="item-info">
                        <div class="seller-info">
                            <div class="seller-avatar"></div>
                            <span class="seller-name">${product.sellerName}</span>
                        </div>
                        ${followButtonHtml}
                    </div>

                    <!-- 전체를 덮는 투명 링크 -->
                    <a href="/products/${product.id}" class="grid-item-link"></a>
                </article>
            </div>
        `;
    }

    /**
     * 다음 페이지의 상품들을 비동기(AJAX)로 로드합니다.
     */
    function loadProducts() {
        // 이미 로딩 중이거나 더 이상 데이터가 없으면 함수를 즉시 종료
        if (isLoading || noMoreData || currentPage >= totalPages) {
            return;
        }

        isLoading = true;
        $('#loading-spinner').show();

        // API 엔드포인트
        const url = `/api/products/category?primary=${primaryCategory}&secondary=${secondaryCategory}&page=${currentPage}&size=16&sort=regTime,desc`;

        $.get(url, function (response) {
            const products = response.products;
            if (products && products.length > 0) {
                // 받은 상품 데이터로 카드 HTML을 만들어 페이지에 추가
                products.forEach((product, index) => {
                    $('#product-grid').append(createProductCard(product, index));
                });

                currentPage++; // 다음 페이지 번호 준비

                // *** 가장 중요한 연동 부분 ***
                // 새로 추가된 카드들에 그리드 애니메이션을 적용하도록 초기화 함수를 호출합니다.
                if (typeof initGridAnimations === 'function') {
                    initGridAnimations();
                }
            }

            // 서버가 마지막 페이지라고 알려주면, 더 이상 로드하지 않도록 설정
            if (response.last) {
                noMoreData = true;
            }

        }).fail(function (xhr, status, error) {
            console.error("상품 로딩 중 오류 발생:", status, error);
            noMoreData = true; // 오류 발생 시 더 이상 로드 시도 안 함
        }).always(function () {
            // 로딩 스피너를 숨기고 로딩 상태를 해제
            $('#loading-spinner').hide();
            isLoading = false;
        });
    }

    // 윈도우 스크롤 이벤트에 loadProducts 함수를 연결
    /*$(window).scroll(function () {
        // 페이지 끝에서 300px 위 지점에 도달하면 다음 페이지 로드
        if ($(window).scrollTop() + $(window).height() > $(document).height() - 300) {
            loadProducts();
        }
    });*/

    // --- 👇 이걸로 교체 ---

    // 1. 스크롤이 발생하는 실제 컨테이너를 변수로 지정합니다.
    //    만약 찾은 요소가 class="main-content" 라면 -> $('.main-content')
    //    만약 찾은 요소가 id="wrapper" 라면 -> $('#wrapper')
    //    만약 정말로 window가 스크롤되는게 맞다면 -> $(window)
    const scrollContainer = $(window); // 기본값은 window, 실제 요소에 맞게 수정하세요!

    scrollContainer.scroll(function () {
        // 스크롤 위치 계산식을 컨테이너에 맞게 수정해야 할 수도 있습니다.
        // 하지만 대부분의 경우 아래 코드가 잘 작동합니다.

        // this는 이벤트가 발생한 요소를 가리킵니다 (여기서는 scrollContainer)
        const element = $(this).get(0);

        // 요소의 스크롤 가능한 전체 높이
        const scrollHeight = element.scrollHeight;
        // 요소의 보이는 부분의 높이
        const clientHeight = element.clientHeight;
        // 스크롤된 높이
        const scrollTop = element.scrollTop;

        // window의 경우, 위 변수들이 다르게 계산될 수 있으므로 분기 처리
        if (scrollContainer.is($(window))) {
            // window 스크롤 계산
            if ($(window).scrollTop() + $(window).height() > $(document).height() - 300) {
                loadProducts();
            }
        } else {
            // 특정 div 요소의 스크롤 계산
            if (scrollTop + clientHeight > scrollHeight - 300) {
                loadProducts();
            }
        }
    });
});
/*]]>*/