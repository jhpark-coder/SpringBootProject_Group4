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
        "Artwork": ["포토그라피", "일러스트레이션", "스케치", "코믹스"],
        "Graphic-Design": ["타이포그라피", "앨범아트", "로고", "브랜딩", "편집디자인"],
        "Character": ["카툰", "팬아트", "2D 캐릭터", "3D 모델링"],
        "Java": ["Spring/JPA", "네트워크", "알고리즘", "코어 자바"],
        "Frontend": ["HTML/CSS", "JavaScript", "React/Vue", "UI/UX"],
        "Python": ["웹 개발", "데이터 분석", "머신러닝", "자동화"]
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

    function appendBotResponse(title, options, showBackButton) {
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