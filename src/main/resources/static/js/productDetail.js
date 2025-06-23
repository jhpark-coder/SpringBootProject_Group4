document.addEventListener('DOMContentLoaded', () => {
    const detailContainer = document.getElementById('product-detail-container');

    if (!detailContainer) {
        console.error('Product detail container not found!');
        return;
    }

    const productId = window.location.pathname.split('/').pop();

    if (!productId) {
        detailContainer.innerHTML = '<p>상품 ID를 찾을 수 없습니다.</p>';
        return;
    }

    detailContainer.innerHTML = '<p>상품 정보를 불러오는 중입니다...</p>';

    fetch(`/api/products/${productId}`)
        .then(response => {
            if (!response.ok) {
                if(response.status === 404) throw new Error('해당 상품을 찾을 수 없습니다.');
                throw new Error('데이터를 불러오는데 실패했습니다.');
            }
            return response.json();
        })
        .then(product => {
            document.title = product.name; // 브라우저 탭 제목 변경
            detailContainer.innerHTML = `
                <img src="${product.imageUrl || '/images/placeholder.png'}" alt="${product.name}" class="product-detail-img">
                <h1>${product.name}</h1>
                <p><strong>가격:</strong> ${product.price.toLocaleString()}원</p>
                <p><strong>설명:</strong> ${product.description || '상품 설명이 없습니다.'}</p>
                <button>구매하기</button>
            `;
        })
        .catch(error => {
            console.error('Error fetching product detail:', error);
            detailContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
        });
}); 