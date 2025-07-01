/**
 * Chat Buttons Controller
 * 챗봇과 상담원 버튼의 동작을 제어하는 스크립트
 */
document.addEventListener('DOMContentLoaded', function() {
    // 버튼 요소들을 생성
    const chatbotButton = document.createElement('div');
    chatbotButton.className = 'chatbot-btn';
    chatbotButton.innerHTML = '<i class="fas fa-robot"></i>';
    
    const chatButton = document.createElement('div');
    chatButton.className = 'chat-btn';
    chatButton.innerHTML = '<i class="fas fa-comments"></i>';
    
    // body에 버튼들을 추가
    document.body.appendChild(chatbotButton);
    document.body.appendChild(chatButton);
    
    // 버튼 위치 고정 함수
    function fixButtonPositions() {
        // 버튼들의 스타일을 JavaScript로 강제 적용
        const buttonStyles = {
            position: 'fixed',
            visibility: 'visible',
            opacity: '1',
            zIndex: '9999'
        };
        
        Object.assign(chatbotButton.style, buttonStyles, {
            bottom: '120px',
            right: '40px'
        });
        
        Object.assign(chatButton.style, buttonStyles, {
            bottom: '40px',
            right: '40px'
        });
    }
    
    // 페이지 로드 시 즉시 실행
    fixButtonPositions();
    
    // 스크롤 이벤트에서도 위치 유지
    window.addEventListener('scroll', fixButtonPositions);
    
    // 창 크기 변경 시에도 위치 유지
    window.addEventListener('resize', fixButtonPositions);
    
    // 탭 포커스 변경 시에도 위치 유지
    window.addEventListener('visibilitychange', fixButtonPositions);
    
    // --- 챗봇 및 채팅 모달 제어 ---
    const chatbotContainer = document.getElementById('chatbot-container');
    const chatbotBody = document.querySelector('.chatbot-container .chatbot-body');
    const closeChatbotBtn = document.getElementById('close-chatbot-btn');
    
    // 카테고리 데이터
    const categories = {
        "Artwork": ["포토그래피", "일러스트레이션", "스케치", "코믹스"],
        "Graphic-Design": ["타이포그래피", "앨범아트", "로고", "브랜딩", "편집디자인"],
        "Character": ["카툰", "팬아트", "2D 캐릭터", "3D 모델링"],
        "Frontend": ["HTML/CSS", "JavaScript", "React/Vue", "UI/UX"],
        "Python": ["웹 개발", "데이터 분석", "머신러닝", "자동화"],
        "Java": ["Spring/JPA", "네트워크", "알고리즘", "코어 자바"],
    };
    
    // Store the initial HTML for reset
    const initialScreenHtml = chatbotBody.innerHTML;

    function resetToInitialScreen() {
        chatbotBody.innerHTML = initialScreenHtml;
        setUniformButtonWidths(chatbotBody);
        addOptionClickListeners();
    }

    function scrollChatToBottom() {
        setTimeout(() => { chatbotBody.scrollTop = chatbotBody.scrollHeight; }, 0);
    }

    function setUniformButtonWidths(container) {
        const optionContainer = container.querySelector('.chatbot-options');
        if (!optionContainer) return;
        
        const buttons = optionContainer.querySelectorAll('.chatbot-option-btn');
        if (buttons.length === 0) return;

        let maxWidth = 0;
        buttons.forEach(button => {
            button.style.width = 'auto';
        });

        buttons.forEach(button => {
            const buttonWidth = button.getBoundingClientRect().width;
            if (buttonWidth > maxWidth) {
                maxWidth = buttonWidth;
            }
        });

        buttons.forEach(button => {
            button.style.width = `${maxWidth}px`;
        });
    }

    function appendUserMessage(text) {
        const optionsContainer = chatbotBody.querySelector('.chatbot-options');
        if (optionsContainer) {
            optionsContainer.remove();
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = 'user-choice-bubble';
        const p = document.createElement('p');
        p.textContent = text;
        messageDiv.appendChild(p);
        
        chatbotBody.appendChild(messageDiv);
        scrollChatToBottom();
    }

    function appendBotResponse(title, options, showBackButton, callback) {
        const botResponseContainer = document.createElement('div');
        botResponseContainer.className = 'bot-response';

        const titleBubble = document.createElement('div');
        titleBubble.className = 'chat-bubble';
        const p = document.createElement('p');
        p.textContent = title;
        titleBubble.appendChild(p);
        botResponseContainer.appendChild(titleBubble);

        if (options && options.length > 0) {
            const optionsContainer = document.createElement('div');
            optionsContainer.className = 'chatbot-options';

            options.forEach(option => {
                const button = document.createElement('button');
                button.className = 'chatbot-option-btn';
                button.dataset.option = option.key;
                button.innerHTML = `${option.icon} ${option.text}`;
                optionsContainer.appendChild(button);
            });

            if (showBackButton) {
                const backButton = document.createElement('button');
                backButton.className = 'chatbot-option-btn';
                backButton.dataset.option = 'back';
                backButton.innerHTML = `<i class="fas fa-arrow-left"></i> 처음으로`;
                optionsContainer.appendChild(backButton);
            }
            botResponseContainer.appendChild(optionsContainer);
        }

        setTimeout(() => {
            chatbotBody.appendChild(botResponseContainer);
            setUniformButtonWidths(botResponseContainer);
            scrollChatToBottom();
            addOptionClickListeners();
            if (typeof callback === 'function') callback();
        }, 600);
    }
    
    function showPrimaryCategories() {
        const primaryCategories = Object.keys(categories).map(key => ({
            key: key, text: key.replace(/-/g, ' '), icon: '<i class="fas fa-folder"></i>'
        }));
        appendBotResponse("1차 카테고리를 선택해주세요.", primaryCategories, true);
    }

    function showSecondaryCategories(primaryCategory) {
        const secondaryCategories = categories[primaryCategory].map(cat => ({
            key: cat, text: cat, icon: '<i class="fas fa-folder-open"></i>'
        }));
        appendBotResponse(`'${primaryCategory.replace(/-/g, ' ')}'의 2차 카테고리입니다.`, secondaryCategories, true);
    }

    async function showPopularProducts(secondaryCategory) {
        try {
            const response = await fetch(`/api/products/popular?secondaryCategory=${encodeURIComponent(secondaryCategory)}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const products = await response.json();

            if (products.length > 0) {
                appendBotResponse(`'${secondaryCategory}' 카테고리에서 가장 인기있는 상품이에요!`, [], false);
                
                const productCardsHtml = products.map(product => `
                    <a href="/products/${product.id}" class="product-recommend-card">
                        <img src="${product.imageUrl}" alt="${product.name}">
                        <div class="info">
                            <span class="name">${product.name}</span>
                            <span class="author">${product.authorName}</span>
                            <span class="price">${product.price.toLocaleString()}원</span>
                        </div>
                    </a>
                `).join('');
                
                setTimeout(() => {
                    const tempDiv = document.createElement('div');
                    tempDiv.innerHTML = productCardsHtml;
                    while (tempDiv.firstChild) {
                        chatbotBody.appendChild(tempDiv.firstChild);
                    }

                    const yesNoOptions = [
                        { key: 'yes_restart', text: '예', icon: '<i class="fas fa-check"></i>' },
                        { key: 'no_close', text: '아니오', icon: '<i class="fas fa-times"></i>' }
                    ];
                    appendBotResponse("다른 추천이 필요하신가요?", yesNoOptions, false);
                    scrollChatToBottom();
                }, 800);

            } else {
                appendBotResponse(`'${secondaryCategory}' 카테고리에는 아직 추천할 상품이 없어요.`, [], true);
            }
        } catch (error) {
            console.error("Error fetching popular products:", error);
            appendBotResponse("상품을 불러오는 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.", [], true);
        }
    }

    function handleOptionClick(event) {
        const button = event.currentTarget;
        const option = button.dataset.option;
        const optionText = button.innerText.trim();
        
        appendUserMessage(optionText);

        if (categories[option]) {
            showSecondaryCategories(option);
            return;
        }

        for (const key in categories) {
            if (categories[key].includes(option)) {
                showPopularProducts(option);
                return;
            }
        }
        
        switch (option) {
            case 'category': 
                showPrimaryCategories(); 
                break;
            case 'keyword':
                appendBotResponse('원하시는 키워드들을 적어주세요', [], false, showKeywordInput);
                break;
            case 'faq':
                appendBotResponse("해당 기능은 현재 준비 중입니다.", [], true);
                break;
            case 'back': 
                resetToInitialScreen(); 
                break;
            case 'yes_restart':
                resetToInitialScreen();
                break;
            case 'no_close':
                chatbotContainer.style.display = 'none';
                break;
        }
    }

    function showKeywordInput() {
        // 기존 옵션 제거
        const optionsContainer = chatbotBody.querySelector('.chatbot-options');
        if (optionsContainer) optionsContainer.remove();
        // 입력창 추가
        const inputDiv = document.createElement('div');
        inputDiv.style.marginTop = '10px';
        inputDiv.style.display = 'flex';
        inputDiv.style.gap = '8px';
        inputDiv.innerHTML = `
            <input type="text" class="chatbot-keyword-input" placeholder="키워드를 입력하세요" style="flex:1; padding:8px; border-radius:6px; border:1px solid #ccc; font-size:14px;">
            <button class="chatbot-keyword-btn" style="padding:8px 16px; border-radius:6px; background:#007bff; color:#fff; border:none;">입력</button>
        `;
        chatbotBody.appendChild(inputDiv);
        const input = inputDiv.querySelector('input');
        const btn = inputDiv.querySelector('button');
        input.focus();
        // 엔터/버튼 입력 처리
        function submitKeyword() {
            const value = input.value.trim();
            console.log('[DEBUG] 키워드 입력값:', value);
            
            if (!value) {
                console.log('[DEBUG] 입력값이 비어있음');
                return;
            }
            
            if (value.length > 200) {
                appendBotResponse('입력 텍스트가 너무 깁니다. 200자 이내로 입력해주세요.', [], true);
                return;
            }
            
            inputDiv.remove();
            appendUserMessage(value);
            
            // 로딩 메시지 표시
            appendBotResponse('키워드를 분석하고 있습니다...', [], false);
            
            // 1. 명사 추출 API 호출
            console.log('[DEBUG] 명사 추출 API 호출 시작');
            fetch('/api/korean/nouns', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text: value })
            })
            .then(response => {
                console.log('[DEBUG] 명사 추출 응답 상태:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                return response.json();
            })
            .then(nouns => {
                console.log('[DEBUG] 명사 추출 결과:', nouns);
                console.log('[DEBUG] 명사 개수:', nouns ? nouns.length : 'null');
                
                if (!Array.isArray(nouns)) {
                    console.error('[ERROR] 명사 추출 결과가 배열이 아님:', typeof nouns);
                    appendBotResponse('키워드 분석 결과가 올바르지 않습니다.', [], true);
                    return;
                }
                
                if (nouns.length === 0) {
                    console.log('[DEBUG] 추출된 명사가 없음');
                    appendBotResponse('의미있는 키워드(명사)를 추출하지 못했습니다. 다른 키워드로 시도해보세요.', [], true);
                    return;
                }
                
                // 2. 추천 API 호출
                console.log('[DEBUG] 키워드 추천 API 호출 시작. 키워드:', nouns);
                fetch('/api/keyword/recommend', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ keywords: nouns })
                })
                .then(response => {
                    console.log('[DEBUG] 키워드 추천 응답 상태:', response.status);
                    if (!response.ok) {
                        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('[DEBUG] 키워드 추천 결과:', data);
                    
                    if (!data || typeof data !== 'object') {
                        console.error('[ERROR] 추천 결과가 객체가 아님:', typeof data);
                        appendBotResponse('추천 결과를 처리하는 중 오류가 발생했습니다.', [], true);
                        return;
                    }
                    
                    const products = data.products || [];
                    console.log('[DEBUG] 추천 상품 개수:', products.length);
                    
                    if (!Array.isArray(products)) {
                        console.error('[ERROR] products가 배열이 아님:', typeof products);
                        appendBotResponse('추천 결과 형식이 올바르지 않습니다.', [], true);
                        return;
                    }
                    
                    if (products.length === 0) {
                        const retryOptions = [
                            { key: 'keyword', text: '다른 키워드로 검색', icon: '<i class="fas fa-search"></i>' },
                            { key: 'back', text: '처음으로', icon: '<i class="fas fa-arrow-left"></i>' }
                        ];
                        appendBotResponse(`"${nouns.join(', ')}" 키워드와 관련된 작품을 찾지 못했습니다. 다른 키워드로 시도해보세요.`, retryOptions, false);
                        return;
                    }
                    
                    appendBotResponse(`"${nouns.join(', ')}" 키워드로 ${products.length}개의 추천 작품을 찾았습니다!`, [], false);
                    
                    // 카드 형태로 출력
                    const cardsHtml = products.map((product, index) => {
                        // 안전한 데이터 처리
                        const safeProduct = {
                            id: product.id || 0,
                            name: product.name || '제목 없음',
                            authorName: product.authorName || '작가 미상',
                            imageUrl: product.imageUrl || '/static/images/default-product.jpg',
                            description: product.description || '',
                            tags: Array.isArray(product.tags) ? product.tags : [],
                            score: product.score || 0
                        };
                        
                        console.log(`[DEBUG] 상품 ${index + 1}:`, safeProduct);
                        
                        return `
                            <a href="/products/${safeProduct.id}" class="product-recommend-card">
                                <img src="${safeProduct.imageUrl}" alt="${safeProduct.name}" onerror="this.src='/static/images/default-product.jpg'">
                                <div class="info">
                                    <span class="name">${safeProduct.name}</span>
                                    <span class="author">${safeProduct.authorName}</span>
                                    <span class="desc">${safeProduct.description}</span>
                                    <span class="tags">${safeProduct.tags.join(', ')}</span>
                                    <span class="score">추천점수: ${safeProduct.score}</span>
                                </div>
                            </a>
                        `;
                    }).join('');
                    
                    setTimeout(() => {
                        const tempDiv = document.createElement('div');
                        tempDiv.innerHTML = cardsHtml;
                        while (tempDiv.firstChild) {
                            chatbotBody.appendChild(tempDiv.firstChild);
                        }
                        
                        // 추가 옵션 제공
                        const continueOptions = [
                            { key: 'yes_restart', text: '다른 키워드로 검색', icon: '<i class="fas fa-search"></i>' },
                            { key: 'no_close', text: '종료', icon: '<i class="fas fa-times"></i>' }
                        ];
                        appendBotResponse("다른 키워드로 더 검색해보시겠어요?", continueOptions, false);
                        
                        scrollChatToBottom();
                    }, 800);
                })
                .catch((err) => {
                    console.error('[ERROR] 키워드 추천 API 오류:', err);
                    appendBotResponse(`추천 결과 조회 중 오류가 발생했습니다: ${err.message}`, [], true);
                });
            })
            .catch((err) => {
                console.error('[ERROR] 명사 추출 API 오류:', err);
                appendBotResponse(`키워드 분석 중 오류가 발생했습니다: ${err.message}`, [], true);
            });
        }
        input.addEventListener('keydown', e => { if (e.key === 'Enter') submitKeyword(); });
        btn.addEventListener('click', submitKeyword);
    }

    function addOptionClickListeners() {
        const optionButtons = chatbotBody.querySelectorAll('.chatbot-option-btn');
        optionButtons.forEach(btn => {
            btn.addEventListener('click', handleOptionClick, { once: true });
        });
    }
    
    // 클릭 이벤트 처리
    chatbotButton.addEventListener('click', function() {
        if (chatbotContainer) {
            chatbotContainer.style.display = 'flex';
            resetToInitialScreen();
        }
    });

    if (closeChatbotBtn) {
        closeChatbotBtn.addEventListener('click', function() {
            if (chatbotContainer) {
                chatbotContainer.style.display = 'none';
            }
        });
    }
    
    chatButton.addEventListener('click', function() {
        // 상담원 채팅 기능은 layout.html의 jQuery 스크립트에서 처리합니다.
        console.log('상담원 버튼 클릭됨');
    });
}); 