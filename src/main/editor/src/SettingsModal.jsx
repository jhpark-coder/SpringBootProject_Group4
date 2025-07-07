import React, { useState, useEffect, useRef } from 'react';
import './SettingsModal.css';

const CATEGORIES = {
    '아트워크': ['포토그래피', '일러스트레이션', '스케치', '코믹스'],
    '그래픽디자인': ['타이포그라피', '앨범아트', '로고', '브랜딩', '편집디자인'],
    '캐릭터': ['카툰', '팬아트', '2D 캐릭터', '3D 모델링'],
    'Java': ['Spring/JPA', '네트워크', '알고리즘', '코어 자바'],
    '프론트엔드': ['HTML/CSS', 'JavaScript', 'React/Vue', 'UI/UX'],
    'Python': ['웹 개발', '데이터 분석', '머신러닝', '자동화'],
};

const RECOMMENDED_TAGS = {
    '아트워크': ['#감성적인', '#풍경', '#인물', '#동물', '#흑백', '#빈티지', '#판타지', '#SF', '#일상', '#수채화', '#유화', '#디지털페인팅', '#펜드로잉', '#컨셉아트', '#캐리커처', '#웹툰', '#만화', '#스토리보드', '#배경아트', '#게임원화'],
    '그래픽디자인': ['#미니멀리즘', '#모던', '#레트로', '#캘리그라피', '#산세리프', '#세리프', '#커버디자인', '#음악', '#아이덴티티', '#기업브랜딩', '#패키지디자인', '#포스터', '#리플렛', '#북커버', '#인포그래픽', '#UX/UI', '#아이콘', '#픽토그램', '#광고디자인', '#시각디자인'],
    '캐릭터': ['#귀여운', '#실사체', '#SD캐릭터', '#애니메이션', '#게임캐릭터', '#웹툰캐릭터', '#오리지널캐릭터', '#몬스터', '#메카닉', '#이모티콘', '#버츄얼유튜버', '#ZBrush', '#Blender', '#Maya', '#Live2D', '#스파인', '#시트지', '#캐릭터시트', '#일러스트', '#컨셉디자인'],
    'Java': ['#백엔드', '#웹개발', '#SpringBoot', '#SpringSecurity', '#JPA', '#Hibernate', '#QueryDSL', '#객체지향', '#디자인패턴', '#멀티스레딩', '#소켓프로그래밍', '#TCP/IP', '#HTTP', '#REST-API', '#자료구조', '#코딩테스트', '#JVM', '#GC', '#이펙티브자바', '#테스트코드'],
    '프론트엔드': ['#웹퍼블리싱', '#반응형웹', '#CSS-in-JS', '#SASS', '#TypeScript', '#ES6+', '#상태관리', '#Redux', '#MobX', '#Vuex', '#Next_js', '#Nuxt_js', '#Webpack', '#Babel', '#웹소켓', '#PWA', '#웹접근성', '#웹표준', '#프로토타이핑', '#Figma'],
    'Python': ['#Django', '#Flask', '#FastAPI', '#Pandas', '#NumPy', '#Matplotlib', '#Scikit-learn', '#TensorFlow', '#PyTorch', '#Jupyter', '#크롤링', '#웹스크레이핑', '#Selenium', '#BeautifulSoup', '#업무자동화', '#RPA', '#데이터시각화', '#딥러닝', '#자연어처리', '#컴퓨터비전'],
};

const AUCTION_DURATIONS = ['1일', '3일', '7일'];

const SettingsModal = ({ isOpen, onClose, onSave, initialSettings }) => {
    const [currentSettings, setCurrentSettings] = useState(initialSettings || {});
    const [primaryCategory, setPrimaryCategory] = useState('');
    const [saleType, setSaleType] = useState(''); // 'sale' 또는 'auction'
    const [salePrice, setSalePrice] = useState('');
    const [auctionDuration, setAuctionDuration] = useState('');
    const [startBidPrice, setStartBidPrice] = useState('');
    const [buyNowPrice, setBuyNowPrice] = useState('');
    const [workDescription, setWorkDescription] = useState('');
    const [buyout_price, setBuyout_price] = useState('');
    const [selectedTags, setSelectedTags] = useState([]);
    const [tagInput, setTagInput] = useState('');

    // 에러 상태 추가
    const [errors, setErrors] = useState({});

    const fileInputRef = useRef(null);

    useEffect(() => {
        console.log('Received initialSettings:', initialSettings); // 데이터 확인을 위한 콘솔 로그 추가
        if (isOpen && initialSettings) {
            // 모든 카테고리 및 서브카테고리 목록 준비
            const allPrimaryCategories = Object.keys(CATEGORIES);
            const allSubCategories = Object.values(CATEGORIES).flat();

            // 1. 카테고리 설정
            const foundPrimary = allPrimaryCategories.find(cat => initialSettings.tags?.includes(cat)) || '';
            const foundSecondary = allSubCategories.find(sub => initialSettings.tags?.includes(sub)) || '';
            
            setPrimaryCategory(foundPrimary);

            // 2. 태그 설정 (카테고리 제외)
            const pureTags = initialSettings.tags?.filter(tag => 
                !allPrimaryCategories.includes(tag) && 
                !allSubCategories.includes(tag)
            ).map(tag => tag.startsWith('#') ? tag : `#${tag}`) || []; // # 기호 정규화

            setSelectedTags(pureTags);

            // 3. currentSettings 상태 업데이트 (가장 중요)
            // 2차 카테고리가 있는 경우, tags 배열에 [1차, 2차] 형식으로 설정
            const categoryTags = foundPrimary ? [foundPrimary] : [];
            if (foundSecondary) {
                categoryTags.push(foundSecondary);
            }

            setCurrentSettings({
                ...initialSettings,
                tags: categoryTags // 카테고리 태그만 설정
            });
            
            // 4. 나머지 상태 설정
            setSaleType(initialSettings.saleType || '');
            setSalePrice(initialSettings.salePrice || '');
            setAuctionDuration(initialSettings.auctionDuration || '');
            setStartBidPrice(initialSettings.startBidPrice || '');
            setBuyNowPrice(initialSettings.buyNowPrice || '');
            setWorkDescription(initialSettings.workDescription || '');
            setBuyout_price(initialSettings.buyout_price || '');
        }
    }, [isOpen, initialSettings]);

    useEffect(() => {
        const foundCategory = Object.keys(CATEGORIES).find(cat => currentSettings.tags.includes(cat));
        if (foundCategory) {
            setPrimaryCategory(foundCategory);
        }
    }, [currentSettings.tags]);

    // settings가 변경될 때 판매방식과 가격 정보를 초기화
    useEffect(() => {
        console.log('SettingsModal 초기화 - settings:', currentSettings);
        if (currentSettings.saleType) {
            setSaleType(currentSettings.saleType);
        }
        if (currentSettings.salePrice) {
            setSalePrice(currentSettings.salePrice);
        }
        if (currentSettings.auctionDuration) {
            setAuctionDuration(currentSettings.auctionDuration);
        }
        if (currentSettings.startBidPrice) {
            setStartBidPrice(currentSettings.startBidPrice);
        }
        if (currentSettings.buyNowPrice) {
            setBuyNowPrice(currentSettings.buyNowPrice);
        }
    }, [currentSettings]);

    // 유효성 검사 함수
    const validateForm = () => {
        const newErrors = {};

        // 기본 필수 필드 검사
        if (!currentSettings.title?.trim()) {
            newErrors.title = '프로젝트 제목은 필수입니다.';
        }

        if (!primaryCategory) {
            newErrors.primaryCategory = '1차 카테고리를 선택해주세요.';
        }

        // 2차 카테고리 검사
        const hasSecondaryCategory = currentSettings.tags.some(tag =>
            primaryCategory && CATEGORIES[primaryCategory]?.includes(tag)
        );
        if (!hasSecondaryCategory) {
            newErrors.secondaryCategory = '세부 카테고리를 선택해주세요.';
        }

        // 판매/경매 관련 검사
        if (hasSecondaryCategory && !saleType) {
            newErrors.saleType = '판매 방식을 선택해주세요.';
        }

        if (saleType === 'sale') {
            if (!salePrice || parseFloat(salePrice) <= 0) {
                newErrors.salePrice = '희망판매가를 입력해주세요.';
            }
        }

        if (saleType === 'auction') {
            if (!auctionDuration) {
                newErrors.auctionDuration = '경매 기간을 선택해주세요.';
            }
            if (!startBidPrice || parseFloat(startBidPrice) <= 0) {
                newErrors.startBidPrice = '시작입찰가를 입력해주세요.';
            }
            if (!buyNowPrice || parseFloat(buyNowPrice) <= 0) {
                newErrors.buyNowPrice = '즉시입찰가를 입력해주세요.';
            }
            if (startBidPrice && buyNowPrice && parseFloat(buyNowPrice) <= parseFloat(startBidPrice)) {
                newErrors.buyNowPrice = '즉시입찰가는 시작입찰가보다 높아야 합니다.';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleTitleChange = (e) => {
        setCurrentSettings(prev => ({ ...prev, title: e.target.value }));
        // 에러 제거
        if (errors.title) {
            setErrors(prev => ({ ...prev, title: '' }));
        }
    };

    const handleCoverImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setCurrentSettings(prev => ({ ...prev, coverImage: reader.result }));
            };
            reader.readAsDataURL(file);
        }
    };

    const handlePrimaryCategoryClick = (category) => {
        setPrimaryCategory(category);
        setCurrentSettings(prev => ({ ...prev, tags: [category] }));
        // 에러 제거
        if (errors.primaryCategory) {
            setErrors(prev => ({ ...prev, primaryCategory: '' }));
        }
    };

    const handleSecondaryCategoryClick = (subCategory) => {
        // 현재 선택된 2차 카테고리 찾기
        const currentSecondaryCategory = currentSettings.tags.find(tag =>
            primaryCategory && CATEGORIES[primaryCategory]?.includes(tag)
        );

        // 클릭된 2차 카테고리가 이미 선택된 상태이면 선택 해제, 아니면 새로 선택
        if (currentSecondaryCategory === subCategory) {
            // 선택 해제: 1차 카테고리만 남김
            setCurrentSettings(prev => ({ ...prev, tags: [primaryCategory] }));
        } else {
            // 새로 선택: 1차 카테고리와 새 2차 카테고리
            setCurrentSettings(prev => ({ ...prev, tags: [primaryCategory, subCategory] }));
        }

        // 에러 제거
        if (errors.secondaryCategory) {
            setErrors(prev => ({ ...prev, secondaryCategory: '' }));
        }
    };

    const handleSaleTypeChange = (type) => {
        setSaleType(type);
        // 판매 타입이 변경되면 관련 데이터 초기화
        if (type === 'sale') {
            setAuctionDuration('');
            setStartBidPrice('');
            setBuyNowPrice('');
        } else if (type === 'auction') {
            setSalePrice('');
        }
        // 에러 제거
        if (errors.saleType) {
            setErrors(prev => ({ ...prev, saleType: '' }));
        }
    };

    const handleSalePriceChange = (e) => {
        const value = e.target.value;
        setSalePrice(value);
        // 에러 제거
        if (errors.salePrice) {
            setErrors(prev => ({ ...prev, salePrice: '' }));
        }
    };

    const handleAuctionDurationChange = (duration) => {
        setAuctionDuration(duration);
        // 에러 제거
        if (errors.auctionDuration) {
            setErrors(prev => ({ ...prev, auctionDuration: '' }));
        }
    };

    const handleStartBidPriceChange = (e) => {
        const value = e.target.value;
        setStartBidPrice(value);

        // 시작입찰가가 즉시입찰가보다 높아진 경우 즉시입찰가 초기화
        if (value && buyNowPrice && parseFloat(value) > parseFloat(buyNowPrice)) {
            setBuyNowPrice('');
        }

        // 에러 제거
        if (errors.startBidPrice) {
            setErrors(prev => ({ ...prev, startBidPrice: '' }));
        }
        if (errors.buyNowPrice) {
            setErrors(prev => ({ ...prev, buyNowPrice: '' }));
        }
    };

    const handleBuyNowPriceChange = (e) => {
        const value = e.target.value;
        setBuyNowPrice(value);

        // 에러 제거
        if (errors.buyNowPrice) {
            setErrors(prev => ({ ...prev, buyNowPrice: '' }));
        }
    };

    const handleTagInputChange = (e) => {
        setTagInput(e.target.value);
    };

    const handleTagKeyDown = (e) => {
        if (e.key === 'Enter' && tagInput.trim() !== '') {
            e.preventDefault();
            addTag(tagInput.trim());
            setTagInput('');
        }
    };

    const addTag = (tag) => {
        // # 기호 정규화
        const newTag = tag.startsWith('#') ? tag : `#${tag}`;

        // 태그가 이미 존재하거나, 카테고리/서브카테고리인 경우 추가하지 않음
        const allCategories = Object.keys(CATEGORIES);
        const allSubCategories = Object.values(CATEGORIES).flat();

        if (selectedTags.length < 5 &&
            !selectedTags.includes(newTag) &&
            !allCategories.includes(newTag) &&
            !allSubCategories.includes(newTag)) {
            setSelectedTags([...selectedTags, newTag]);
        }
    };

    const removeTag = (tagToRemove) => {
        // 카테고리나 서브카테고리가 아닌 경우에만 제거 가능
        const allCategories = Object.keys(CATEGORIES);
        const allSubCategories = Object.values(CATEGORIES).flat();
        
        if (!allCategories.includes(tagToRemove) && 
            !allSubCategories.includes(tagToRemove)) {
            setSelectedTags(selectedTags.filter(tag => tag !== tagToRemove));
        }
    };

    const handleWorkDescriptionChange = (e) => {
        setWorkDescription(e.target.value);
    };

    const handleSave = () => {
        if (validateForm()) {
            // 현재 선택된 카테고리 (1차, 2차)와 사용자가 추가한 태그를 모두 합침
            const finalTags = [...currentSettings.tags, ...selectedTags];

            const settings = {
                ...currentSettings,
                tags: finalTags,
                primaryCategory,
                saleType,
                salePrice: saleType === 'sale' ? salePrice : null,
                auctionDuration: saleType === 'auction' ? auctionDuration : null,
                startBidPrice: saleType === 'auction' ? startBidPrice : null,
                buyNowPrice: saleType === 'auction' ? buyNowPrice : null,
                workDescription
            };

            onSave(settings);
            onClose();
        }
    };

    // 2차 카테고리가 선택되었는지 확인
    const hasSecondaryCategory = currentSettings.tags.some(tag =>
        primaryCategory && CATEGORIES[primaryCategory]?.includes(tag)
    );

    // 추천 태그 선택 여부 확인 함수 추가
    const isTagSelected = (tag) => {
        return selectedTags.includes(tag);
    };

    const handleRecommendedTagClick = (tag) => {
        if (isTagSelected(tag)) {
            removeTag(tag);
        } else if (selectedTags.length < 5) {
            addTag(tag);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="settings-modal-content" onClick={e => e.stopPropagation()}>
                <div className="settings-modal-header">
                    <h2>Settings</h2>
                </div>
                <div className="modal-body">
                    <div className="settings-grid">
                        <div className="left-column">
                            <div className="form-group">
                                <label>프로젝트 표지</label>
                                <div className="cover-image-container">
                                    {currentSettings.coverImage ? (
                                        <div className="image-wrapper" onClick={() => fileInputRef.current.click()}>
                                            <img src={currentSettings.coverImage} alt="프로젝트 표지" className="main-image" />
                                            <div className="image-overlay">
                                                <button className="overlay-button">이미지 변경</button>
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="cover-image-placeholder" onClick={() => fileInputRef.current.click()}>
                                            <span>+</span>
                                            <p>클릭하여 표지 이미지 추가</p>
                                        </div>
                                    )}
                                    <input
                                        type="file"
                                        ref={fileInputRef}
                                        onChange={handleCoverImageChange}
                                        accept="image/*"
                                        style={{ display: 'none' }}
                                    />
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="work-description">작품 설명</label>
                                <textarea
                                    id="work-description"
                                    value={workDescription}
                                    onChange={handleWorkDescriptionChange}
                                    placeholder="작품에 대한 추가적인 설명을 자유롭게 작성해주세요. (예: 제작 과정, 사용된 툴, 작품의 의미 등)"
                                    className="work-description-textarea"
                                />
                            </div>

                            <div className="form-group">
                                <label>선택된 태그</label>
                                <div className="selected-tags">
                                    {selectedTags.map(tag => (
                                        <div key={tag} className="tag-item">
                                            {tag}
                                            <button onClick={() => removeTag(tag)} className="remove-tag-btn">×</button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                        <div className="right-column">
                            <div className="form-group">
                                <label htmlFor="project-title">프로젝트 제목 (필수)</label>
                                <input
                                    type="text"
                                    id="project-title"
                                    value={currentSettings.title}
                                    onChange={handleTitleChange}
                                    placeholder="프로젝트의 제목을 입력하세요"
                                    className={errors.title ? 'error' : ''}
                                />
                                {errors.title && <span className="error-message">{errors.title}</span>}
                            </div>

                            <div className="form-group">
                                <label>1차 카테고리</label>
                                <div className="category-group primary-categories">
                                    {Object.keys(CATEGORIES).map(cat => (
                                        <button
                                            key={cat}
                                            className={`category-button ${primaryCategory === cat ? 'active' : ''}`}
                                            onClick={() => handlePrimaryCategoryClick(cat)}
                                        >
                                            {cat}
                                        </button>
                                    ))}
                                </div>
                                {errors.primaryCategory && <span className="error-message">{errors.primaryCategory}</span>}

                                {primaryCategory && (
                                    <div className="form-group">
                                        <label>세부 카테고리</label>
                                        <div className="category-group secondary-categories">
                                            {CATEGORIES[primaryCategory].map(subCat => (
                                                <button
                                                    key={subCat}
                                                    className={`category-button ${currentSettings.tags.includes(subCat) ? 'active' : ''}`}
                                                    onClick={() => handleSecondaryCategoryClick(subCat)}
                                                >
                                                    {subCat}
                                                </button>
                                            ))}
                                        </div>
                                        {errors.secondaryCategory && <span className="error-message">{errors.secondaryCategory}</span>}
                                    </div>
                                )}
                            </div>

                            {/* 2차 카테고리가 선택되면 판매 방식 섹션 표시 */}
                            {hasSecondaryCategory && (
                                <div className="form-section">
                                    <h3 className="section-title">판매 방식</h3>
                                    <div className="sale-type-group">
                                        <button
                                            className={`sale-type-button ${saleType === 'sale' ? 'active' : ''}`}
                                            onClick={() => handleSaleTypeChange('sale')}
                                        >
                                            판매
                                        </button>
                                        <button
                                            className={`sale-type-button ${saleType === 'auction' ? 'active' : ''}`}
                                            onClick={() => handleSaleTypeChange('auction')}
                                        >
                                            경매
                                        </button>
                                    </div>
                                    {errors.saleType && <span className="error-message">{errors.saleType}</span>}
                                </div>
                            )}

                            {saleType === 'sale' && (
                                <div className="price-input-section">
                                    <label htmlFor="sale-price">희망판매가 (원)</label>
                                    <input
                                        type="number"
                                        id="sale-price"
                                        value={salePrice}
                                        onChange={handleSalePriceChange}
                                        placeholder="희망판매가를 입력하세요"
                                        min="0"
                                        className={errors.salePrice ? 'error' : ''}
                                    />
                                    {errors.salePrice && <span className="error-message">{errors.salePrice}</span>}
                                </div>
                            )}

                            {saleType === 'auction' && (
                                <div className="auction-settings">
                                    <div className="auction-duration-section">
                                        <label>경매 기간</label>
                                        <div className="duration-group">
                                            {AUCTION_DURATIONS.map(duration => (
                                                <button
                                                    key={duration}
                                                    className={`duration-button ${auctionDuration === duration ? 'active' : ''}`}
                                                    onClick={() => handleAuctionDurationChange(duration)}
                                                >
                                                    {duration}
                                                </button>
                                            ))}
                                        </div>
                                        {errors.auctionDuration && <span className="error-message">{errors.auctionDuration}</span>}
                                    </div>

                                    <div className="price-input-section">
                                        <label htmlFor="start-bid-price">시작입찰가 (원)</label>
                                        <input
                                            type="number"
                                            id="start-bid-price"
                                            value={startBidPrice}
                                            onChange={handleStartBidPriceChange}
                                            placeholder="시작입찰가를 입력하세요"
                                            min="0"
                                            className={errors.startBidPrice ? 'error' : ''}
                                        />
                                        {errors.startBidPrice && <span className="error-message">{errors.startBidPrice}</span>}
                                    </div>

                                    <div className="price-input-section">
                                        <label htmlFor="buy-now-price">즉시입찰가 (원)</label>
                                        <input
                                            type="number"
                                            id="buy-now-price"
                                            value={buyNowPrice}
                                            onChange={handleBuyNowPriceChange}
                                            placeholder="즉시입찰가는 시작입찰가보다 높아야 합니다."
                                            min="0"
                                            className={errors.buyNowPrice ? 'error' : ''}
                                        />
                                        {errors.buyNowPrice && <span className="error-message">{errors.buyNowPrice}</span>}
                                    </div>
                                </div>
                            )}

                            {/* 태그 섹션 */}
                            {hasSecondaryCategory && (
                                <div className="form-section">
                                    <h3 className="section-title">태그</h3>
                                    <div className="tag-wrapper">
                                        <div className="tag-input-container">
                                            <input
                                                type="text"
                                                value={tagInput}
                                                onChange={handleTagInputChange}
                                                onKeyDown={handleTagKeyDown}
                                                placeholder="태그를 입력하고 Enter (최대 5개)"
                                                className="tag-input"
                                                disabled={selectedTags.length >= 5}
                                            />
                                        </div>
                                        <div className="recommended-tags-container">
                                            <h4 className="recommended-tags-title">추천 태그</h4>
                                            <div className="recommended-tags">
                                                {RECOMMENDED_TAGS[primaryCategory]?.map(tag => (
                                                    <button 
                                                        key={tag} 
                                                        onClick={() => handleRecommendedTagClick(tag)} 
                                                        className={`recommended-tag-btn ${isTagSelected(tag) ? 'selected' : ''}`}
                                                        disabled={selectedTags.length >= 5 && !isTagSelected(tag)}
                                                    >
                                                        {tag}
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
                <div className="settings-modal-footer">
                    <button onClick={handleSave} className="save-button">저장</button>
                    <button onClick={onClose} className="cancel-button">취소</button>
                </div>
            </div>
        </div>
    );
};

export default SettingsModal; 