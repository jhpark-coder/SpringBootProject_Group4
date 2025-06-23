document.addEventListener('DOMContentLoaded', () => {
    const mainContainer = document.getElementById('main-content-container');

    if (!mainContainer) {
        console.error('Main content container not found!');
        return;
    }

    mainContainer.innerHTML = '<h1>Nexus에 오신 것을 환영합니다!</h1><p>상품 목록을 불러오는 중입니다...</p>';

    fetch('/api/products')
        .then(response => {
            if (!response.ok) throw new Error('데이터를 불러오는데 실패했습니다.');
            return response.json();
        })
        .then(products => {
            mainContainer.innerHTML = '<h1>Nexus에 오신 것을 환영합니다!</h1>'; // 기존 로딩 메시지 제거

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