'use strict';

/**
 * '좋아요' 버튼의 상태를 토글하고 서버에 변경 사항을 전송합니다.
 * @param {HTMLElement} button - 클릭된 '좋아요' 버튼 요소
 */
function toggleLike(button) {
    const csrfToken = $("meta[name='_csrf']").attr("content");
    const csrfHeader = $("meta[name='_csrf_header']").attr("content");

    const productId = button.dataset.productId;
    if (!productId) {
        console.error('Product ID not found on the button.');
        return;
    }

    $.ajax({
        url: `/api/products/${productId}/heart`,
        type: 'POST',
        beforeSend: function (xhr) {
            if (csrfHeader && csrfToken) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        },
        success: function (data) {
            const likeCountSpan = button.querySelector('.like-count');
            if (likeCountSpan) {
                likeCountSpan.textContent = data.heartCount;
            }
            button.classList.toggle('liked', data.isLiked);
        },
        error: function (xhr) {
            if (xhr.status === 401) {
                alert('로그인이 필요합니다.');
            } else {
                alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            }
            console.error('좋아요 처리 실패:', xhr.responseText);
        }
    });
}

/**
 * 주어진 상품 데이터로 상세 페이지의 HTML 콘텐츠를 생성합니다.
 * @param {object} product - 상품 데이터 객체
 * @returns {string} - 생성된 HTML 문자열
 */
function createProductDetailHtml(product) {
    // Thymeleaf와 달리 JS에서는 'null' 이나 'undefined'를 직접 체크해야 합니다.
    const createdBy = product.createdBy || '익명 작가';
    const primaryCategory = product.primaryCategory || '';
    const secondaryCategory = product.secondaryCategory || '';
    const heartCount = product.productHearts ? product.productHearts.length : 0;
    const imageUrl = product.imageUrl || 'https://via.placeholder.com/800x600/cccccc/666666?text=이미지+없음';
    const description = product.description || '<p>상품 설명이 없습니다.</p>';

    // 로그인 상태는 서버에서 렌더링하지 않으므로, 버튼 노출 여부는 JS에서 판단할 수 없습니다.
    // 대신, 모든 사용자에게 구독 버튼을 일단 보여주고, 클릭 시 서버에서 권한을 체크하도록 하는 것이 일반적입니다.
    // 여기서는 th:if 조건을 제거하고 버튼을 항상 표시합니다.
    const subscribeButtonHtml = `<button class="subscribe-btn">Subscribe</button>`;

    return `
        <main class="main-content">
            <section class="artwork-section">
                <h1>${product.name}</h1>
                <img src="${imageUrl}" alt="${product.name}" class="artwork-image" onerror="this.src='https://via.placeholder.com/800x600/cccccc/666666?text=이미지+오류'">
                <div class="description-section">${description}</div>
            </section>
            <aside class="sidebar">
                <div class="author-info">
                    <div class="author-profile">
                        <img src="https://via.placeholder.com/40" alt="작가 프로필" class="author-pfp">
                        <span class="author-name">${createdBy}</span>
                    </div>
                    ${subscribeButtonHtml}
                </div>
                <div class="tags">
                    ${primaryCategory ? `<button class="tag-btn">${primaryCategory}</button>` : ''}
                    ${secondaryCategory ? `<button class="tag-btn">${secondaryCategory}</button>` : ''}
                </div>
                <p class="description">
                    이곳은 작품에 대한 간략한 설명이 들어가는 영역입니다.
                </p>
                <div class="sidebar-actions">
                    <button class="like-btn" data-product-id="${product.id}" onclick="toggleLike(this)">
                        <span class="like-icon">👍</span>
                        <span class="like-count">${heartCount}</span>
                    </button>
                    <button class="continue-btn" onclick="alert('포인트 결제 기능 추가 예정!')">
                        <span>포인트로 계속보기</span>
                    </button>
                </div>
            </aside>
        </main>
    `;
}

/**
 * 현재 사용자의 '좋아요' 상태를 조회하여 버튼에 반영합니다.
 * @param {string} productId - 상품 ID
 */
function checkInitialLikeStatus(productId) {
    const likeBtn = $('.like-btn');
    if (likeBtn.length) {
        $.get(`/api/products/${productId}/heart/status`)
            .done(function (data) {
                likeBtn.toggleClass('liked', data.isLiked);
            })
            .fail(function () {
                console.log('좋아요 상태 조회 실패 (비로그인 또는 서버 오류)');
            });
    }
}


document.addEventListener('DOMContentLoaded', () => {
    const placeholder = document.getElementById('product-detail-placeholder');
    const noJsWarning = document.getElementById('no-js-warning');

    if (!placeholder || !noJsWarning) {
        console.error('필수 요소를 찾을 수 없습니다: placeholder 또는 no-js-warning');
        return;
    }

    // 1. URL에서 상품 ID 추출
    const pathParts = window.location.pathname.split('/');
    const productId = pathParts[pathParts.length - 1];

    if (!productId || isNaN(productId)) {
        placeholder.innerHTML = '<p style="text-align:center; color:red; margin-top: 50px;">잘못된 상품 ID입니다.</p>';
        noJsWarning.style.display = 'none'; // 경고는 숨겨줍니다.
        return;
    }

    // 2. 로딩 메시지 표시
    placeholder.innerHTML = '<p style="text-align:center; margin-top: 50px;">상품 정보를 불러오는 중...</p>';

    // 3. API 호출하여 상품 데이터 가져오기
    fetch(`/api/products/${productId}`)
        .then(response => {
            if (!response.ok) {
                if (response.status === 404) throw new Error('해당 상품을 찾을 수 없습니다.');
                throw new Error(`데이터 로딩 실패 (상태: ${response.status})`);
            }
            return response.json();
        })
        .then(product => {
            // 4. 데이터로 HTML 생성 및 삽입
            const productHtml = createProductDetailHtml(product);
            placeholder.innerHTML = productHtml;

            // 5. 콘텐츠 삽입 후 '좋아요' 상태 확인
            checkInitialLikeStatus(product.id);

            // 6. 브라우저 탭 제목 변경
            document.title = product.name + ' - Nexus';

            // 7. JS 경고 메시지 숨기기
            noJsWarning.style.display = 'none';
        })
        .catch(error => {
            // 8. 오류 처리
            console.error('상품 상세 정보 로딩 중 오류:', error);
            placeholder.innerHTML = `<p style="text-align:center; color:red; margin-top: 50px;">${error.message}</p>`;
            noJsWarning.style.display = 'none';
        });
}); 