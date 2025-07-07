/**
 * Chat Buttons Controller
 * 챗봇과 상담원 버튼의 동작을 제어하는 스크립트
 */
document.addEventListener('pageReady', function () {
    // pageReady 이벤트를 기다려 모든 요소가 안정화된 후 실행
    initializeChatComponents();
});

// DOMContentLoaded도 유지하여 pageReady가 발생하지 않는 경우 대비
document.addEventListener('DOMContentLoaded', function () {
    // pageReady 이벤트가 2초 내에 발생하지 않으면 강제 초기화
    setTimeout(function () {
        if (!window.chatComponentsInitialized) {
            console.warn('pageReady timeout - force initializing chat components');
            initializeChatComponents();
        }
    }, 2000);
});

function initializeChatComponents() {
    // 중복 초기화 방지
    if (window.chatComponentsInitialized) return;
    window.chatComponentsInitialized = true;

    // HTML에 이미 있는 버튼 요소들을 찾기
    const chatbotButton = document.querySelector('.chatbot-btn');
    const chatButton = document.querySelector('.chat-btn');

    if (!chatbotButton || !chatButton) {
        console.error('Chat buttons not found in HTML');
        return;
    }

    // 버튼 표시 및 위치 강제 설정
    requestAnimationFrame(() => {
        // visibility와 opacity로 표시
        chatbotButton.style.visibility = 'visible';
        chatbotButton.style.opacity = '1';
        chatbotButton.style.display = 'flex';

        chatButton.style.visibility = 'visible';
        chatButton.style.opacity = '1';
        chatButton.style.display = 'flex';

        // position: fixed 재확인 및 위치 강제 설정
        chatbotButton.style.setProperty('position', 'fixed', 'important');
        chatbotButton.style.setProperty('right', '40px', 'important');
        chatbotButton.style.setProperty('bottom', '120px', 'important');

        chatButton.style.setProperty('position', 'fixed', 'important');
        chatButton.style.setProperty('right', '40px', 'important');
        chatButton.style.setProperty('bottom', '40px', 'important');

        // 강제 리플로우
        void document.body.offsetHeight;

        // GPU 가속 활성화
        chatbotButton.style.transform = 'translateZ(0)';
        chatButton.style.transform = 'translateZ(0)';

        // 두 번째 프레임에서 추가 스타일 적용
        requestAnimationFrame(() => {
            // 추가 스타일 적용
            const buttonStyle = {
                width: '60px',
                height: '60px',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '24px',
                cursor: 'pointer',
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
                transition: 'transform 0.3s ease, box-shadow 0.3s ease'
            };

            Object.assign(chatbotButton.style, buttonStyle);
            Object.assign(chatButton.style, buttonStyle);

            chatbotButton.style.backgroundColor = '#28a745';
            chatButton.style.backgroundColor = '#007bff';
            chatbotButton.style.color = 'white';
            chatButton.style.color = 'white';

            // 초기화 완료 표시
            chatbotButton.classList.add('chat-button-initialized');
            chatButton.classList.add('chat-button-initialized');
        });
    });

    // 전역 함수로 버튼 초기화 로직을 노출시켜, layout.html의 제어 스크립트가 호출할 수 있도록 함
    window.initializeChatButtons = function () {
        if (chatbotButton && chatButton) {
            chatbotButton.classList.add('chat-button-initialized');
            chatButton.classList.add('chat-button-initialized');

            // 버튼 위치 재계산
            recalculateButtonPositions();
        }
    };

    // 초기 위치 설정
    window.initializeChatButtons();

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
    const initialScreenHtml = chatbotBody ? chatbotBody.innerHTML : '';

    // 강제 리플로우 함수
    function forceReflow() {
        // 강제 리플로우를 트리거하여 레이아웃 재계산
        document.body.style.display = 'none';
        void document.body.offsetHeight; // 강제 리플로우
        document.body.style.display = '';
    }

    // 버튼 위치 재계산 함수
    function recalculateButtonPositions() {
        // ResizeObserver를 사용하여 동적으로 위치 조정
        if (window.ResizeObserver) {
            const resizeObserver = new ResizeObserver(entries => {
                // 위치 재계산이 필요한 경우
                forceReflow();
            });

            if (chatbotButton) resizeObserver.observe(chatbotButton);
            if (chatButton) resizeObserver.observe(chatButton);

            // 초기 관찰 후 해제
            setTimeout(() => {
                resizeObserver.disconnect();
            }, 1000);
        }
    }

    function resetToInitialScreen() {
        if (chatbotBody) {
            chatbotBody.innerHTML = initialScreenHtml;
            setUniformButtonWidths(chatbotBody);
            addOptionClickListeners();
        }
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
                button.type = 'button'; // 반드시 명시!
                button.className = 'chatbot-option-btn';
                button.dataset.option = option.key;
                button.innerHTML = `${option.icon} ${option.text}`;
                optionsContainer.appendChild(button);
            });

            if (showBackButton) {
                const backButton = document.createElement('button');
                backButton.type = 'button'; // 반드시 명시!
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
                            <span class="seller">${product.sellerName}</span>
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
                    // 강제 리플로우 코드는 화면을 최상단으로 이동시키는 부작용이 있어 제거합니다.
                    // setTimeout(() => {
                    //     document.body.style.display = 'none';
                    //     void document.body.offsetHeight;
                    //     document.body.style.display = '';
                    // }, 0);

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
        event.preventDefault();
        event.stopPropagation();
        const button = event.currentTarget;
        const option = button.dataset.option;
        const optionText = button.innerText.trim();
        // FAQ 질문 버튼 클릭 시
        if (option && option.startsWith('faq_q_')) {
            const faqId = parseInt(option.replace('faq_q_', ''));
            const faqs = window._currentFaqList || [];
            const faq = faqs.find(f => f.id === faqId);
            if (faq) {
                appendUserMessage(faq.question);
                setTimeout(() => {
                    appendBotResponse(faq.answer, [
                        { key: 'faq_connect_agent', text: '상담사 연결', icon: '<i class="fas fa-headset"></i>' },
                        { key: 'faq_back_to_list', text: '처음으로', icon: '<i class="fas fa-arrow-left"></i>' }
                    ], false);
                }, 500);
            }
            return;
        }
        // 상담사 연결 버튼 처리
        if (option === 'faq_connect_agent') {
            // 챗봇 모달 닫기 (두 방식 모두)
            const chatbotContainer = document.getElementById('chatbot-container');
            if (chatbotContainer) chatbotContainer.style.display = 'none';
            if (window.chatbotContainer) window.chatbotContainer.style.display = 'none';
            // 상담사 모달 열기
            const chatContainer = document.getElementById('chat-container');
            if (chatContainer) chatContainer.style.display = 'flex';
            return;
        }
        // '처음으로' 버튼 처리
        if (option === 'faq_back_to_list') {
            resetToInitialScreen();
            return;
        }
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
                showFaqCategories();
                break;
            case 'faq_search':
                showFaqSearch();
                break;
            case 'faq_popular':
                showPopularFaqs();
                break;
            case 'faq_categories':
                showFaqCategories();
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
            default:
                // FAQ 카테고리 선택 처리
                if (option.startsWith('faq_category_')) {
                    const category = option.replace('faq_category_', '');
                    showFaqsByCategory(category);
                }
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
                                    { key: 'back', text: '처음으로', icon: '<i class="fas fa-home"></i>' }
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
                                    sellerName: product.sellerName || '판매자 미상',
                                    imageUrl: product.imageUrl || '/static/images/default-product.jpg',
                                    description: product.description || '',
                                    tags: Array.isArray(product.tags) ? product.tags : [],
                                    finalScore: product.finalScore || 0
                                };

                                console.log(`[DEBUG] 상품 ${index + 1}:`, safeProduct);

                                return `
                            <a href="/products/${safeProduct.id}" class="product-recommend-card">
                                <img src="${safeProduct.imageUrl}" alt="${safeProduct.name}" onerror="this.src='/static/images/default-product.jpg'">
                                <div class="info">
                                    <span class="name">${safeProduct.name}</span>
                                    <span class="seller">${safeProduct.sellerName}</span>
                                    <span class="desc">${safeProduct.description}</span>
                                    <span class="tags">${safeProduct.tags.join(', ')}</span>
                                    <span class="score">추천점수: ${safeProduct.finalScore ? safeProduct.finalScore.toFixed(1) : '0.0'}</span>
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
                                    { key: 'back', text: '처음으로', icon: '<i class="fas fa-home"></i>' },
                                    { key: 'no_close', text: '종료', icon: '<i class="fas fa-times"></i>' }
                                ];
                                appendBotResponse("다른 추천을 원하시면 처음으로 돌아가주세요.", continueOptions, false);

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

    // FAQ 관련 함수들
    async function showFaqCategories() {
        try {
            const response = await fetch('/api/faq/categories');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const categories = await response.json();

            const categoryOptions = categories.map(category => ({
                key: `faq_category_${category}`,
                text: category,
                icon: '<i class="fas fa-folder"></i>'
            }));

            appendBotResponse("어떤 카테고리의 질문을 찾고 계신가요?", categoryOptions, true);
        } catch (error) {
            console.error("Error fetching FAQ categories:", error);
            appendBotResponse("FAQ 카테고리를 불러오는 중 오류가 발생했습니다.", [], true);
        }
    }

    async function showFaqsByCategory(category) {
        try {
            const response = await fetch(`/api/faq/category?name=${encodeURIComponent(category)}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const faqs = await response.json();
            if (faqs.length === 0) {
                appendBotResponse("해당 카테고리에는 아직 FAQ가 없습니다.", [], true);
                return;
            }
            // 질문 목록만 버튼으로 보여주기
            const questionOptions = faqs.map(faq => ({
                key: `faq_q_${faq.id}`,
                text: faq.question,
                icon: '<i class="fas fa-question-circle"></i>'
            }));
            appendBotResponse(`${category} 카테고리의 자주 묻는 질문입니다.`, questionOptions, true);
            // 클릭 이벤트 핸들러에서 질문/답변 대화형 출력 처리
            window._currentFaqList = faqs; // 전역에 저장
        } catch (error) {
            console.error("Error fetching FAQs by category:", error);
            appendBotResponse("FAQ를 불러오는 중 오류가 발생했습니다.", [], true);
        }
    }

    function addFaqCardListeners() {
        const faqCards = chatbotBody.querySelectorAll('.faq-card');
        faqCards.forEach(card => {
            const question = card.querySelector('.faq-question');
            const answer = card.querySelector('.faq-answer');
            const faqId = card.dataset.faqId;

            question.addEventListener('click', () => {
                const isVisible = answer.style.display !== 'none';
                answer.style.display = isVisible ? 'none' : 'block';

                // 조회수 증가
                if (!isVisible) {
                    fetch(`/api/faq/${faqId}/view`, { method: 'POST' })
                        .catch(err => console.error('Error incrementing view count:', err));
                }
            });
        });
    }

    async function showFaqSearch() {
        const inputDiv = document.createElement('div');
        inputDiv.style.marginTop = '10px';
        inputDiv.style.display = 'flex';
        inputDiv.style.gap = '8px';
        inputDiv.innerHTML = `
            <input type="text" class="faq-search-input" placeholder="검색어를 입력하세요" style="flex:1; padding:8px; border-radius:6px; border:1px solid #ccc; font-size:14px;">
            <button class="faq-search-btn" style="padding:8px 16px; border-radius:6px; background:#007bff; color:#fff; border:none;">검색</button>
        `;
        chatbotBody.appendChild(inputDiv);

        const input = inputDiv.querySelector('input');
        const btn = inputDiv.querySelector('button');
        input.focus();

        function submitSearch() {
            const value = input.value.trim();
            if (!value) return;

            inputDiv.remove();
            appendUserMessage(`"${value}" 검색`);

            // 검색 API 호출
            fetch(`/api/faq/search?keyword=${encodeURIComponent(value)}`)
                .then(response => response.json())
                .then(faqs => {
                    if (faqs.length === 0) {
                        appendBotResponse(`"${value}" 검색 결과가 없습니다.`, [], true);
                        return;
                    }

                    appendBotResponse(`"${value}" 검색 결과 ${faqs.length}개를 찾았습니다.`, [], false);

                    const faqCardsHtml = faqs.map(faq => `
                        <div class="faq-card" data-faq-id="${faq.id}">
                            <div class="faq-question">
                                <i class="fas fa-question-circle"></i>
                                <span>${faq.question}</span>
                                <i class="fas fa-chevron-down"></i>
                            </div>
                            <div class="faq-answer" style="display: none;">
                                <i class="fas fa-info-circle"></i>
                                <span>${faq.answer}</span>
                            </div>
                        </div>
                    `).join('');

                    setTimeout(() => {
                        chatbotBody.insertAdjacentHTML('beforeend', faqCardsHtml);
                        // FAQ 카드 클릭 이벤트 추가
                        addFaqCardListeners();

                        const continueOptions = [
                            { key: 'faq_search', text: '다른 검색', icon: '<i class="fas fa-search"></i>' },
                            { key: 'back', text: '처음으로', icon: '<i class="fas fa-arrow-left"></i>' }
                        ];
                        appendBotResponse("다른 검색을 해보시겠어요?", continueOptions, false);

                        scrollChatToBottom();
                    }, 800);
                })
                .catch(error => {
                    console.error("Error searching FAQs:", error);
                    appendBotResponse("검색 중 오류가 발생했습니다.", [], true);
                });
        }

        input.addEventListener('keydown', e => { if (e.key === 'Enter') submitSearch(); });
        btn.addEventListener('click', submitSearch);
    }

    async function showPopularFaqs() {
        try {
            const response = await fetch('/api/faq/popular');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const faqs = await response.json();

            if (faqs.length === 0) {
                appendBotResponse("인기 FAQ가 없습니다.", [], true);
                return;
            }

            appendBotResponse("가장 많이 찾는 FAQ입니다.", [], false);

            const faqCardsHtml = faqs.map(faq => `
                <div class="faq-card" data-faq-id="${faq.id}">
                    <div class="faq-question">
                        <i class="fas fa-question-circle"></i>
                        <span>${faq.question}</span>
                        <i class="fas fa-chevron-down"></i>
                        <small style="color: #666; margin-left: auto;">조회수: ${faq.viewCount}</small>
                    </div>
                    <div class="faq-answer" style="display: none;">
                        <i class="fas fa-info-circle"></i>
                        <span>${faq.answer}</span>
                    </div>
                </div>
            `).join('');

            setTimeout(() => {
                chatbotBody.insertAdjacentHTML('beforeend', faqCardsHtml);
                // FAQ 카드 클릭 이벤트 추가
                addFaqCardListeners();

                const continueOptions = [
                    { key: 'faq_categories', text: '카테고리별 보기', icon: '<i class="fas fa-folder"></i>' },
                    { key: 'back', text: '처음으로', icon: '<i class="fas fa-arrow-left"></i>' }
                ];
                appendBotResponse("다른 FAQ를 찾아보시겠어요?", continueOptions, false);

                scrollChatToBottom();
            }, 800);

        } catch (error) {
            console.error("Error fetching popular FAQs:", error);
            appendBotResponse("인기 FAQ를 불러오는 중 오류가 발생했습니다.", [], true);
        }
    }

    function addOptionClickListeners() {
        const optionButtons = chatbotBody.querySelectorAll('.chatbot-option-btn');
        optionButtons.forEach(btn => {
            btn.addEventListener('click', handleOptionClick, { once: true });
        });
    }

    // 클릭 이벤트 처리 - 챗봇 창 토글 기능
    chatbotButton.addEventListener('click', function () {
        if (chatbotContainer) {
            const isVisible = chatbotContainer.style.display === 'flex';
            if (isVisible) {
                chatbotContainer.style.display = 'none';
            } else {
                chatbotContainer.style.display = 'flex';
                resetToInitialScreen(); // 챗봇을 열 때 항상 초기 화면으로 리셋
            }
        }
    });

    if (closeChatbotBtn) {
        closeChatbotBtn.addEventListener('click', function () {
            if (chatbotContainer) {
                chatbotContainer.style.display = 'none';
            }
        });
    }

    chatButton.addEventListener('click', function () {
        // 새로운 Socket.IO 기반 채팅 기능 호출
        console.log('상담원 버튼 클릭됨 - Socket.IO 채팅 시작');
        if (typeof openChat === 'function') {
            openChat();
        } else {
            console.warn('openChat 함수를 찾을 수 없습니다. chat.js가 로드되었는지 확인하세요.');
        }
    });
} 