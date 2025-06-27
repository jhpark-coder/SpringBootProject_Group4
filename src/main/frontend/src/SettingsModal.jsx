import React, { useState, useEffect, useRef } from 'react';
import './SettingsModal.css';

const CATEGORIES = {
    '아트워크': ['포토그라피', '일러스트레이션', '스케치', '코믹스'],
    '그래픽디자인': ['타이포그라피', '뮤직패키징', '로고', '그래픽디자인스', '편집'],
    '캐릭터': ['카툰', '애니메', '팬아트', '3D'],
    'Java': ['통신', '알고리즘', 'Thread', 'etc'],
    '프론트엔드': ['HTML', 'CSS', 'Javascript', 'etc'],
    'Python': ['통신', '알고리즘', 'Thread', 'etc'],
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

    // 에러 상태 추가
    const [errors, setErrors] = useState({});

    const fileInputRef = useRef(null);

    useEffect(() => {
        if (isOpen && initialSettings) {
            setCurrentSettings(initialSettings);
            setPrimaryCategory(initialSettings.primaryCategory || '');
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
        const newTags = [primaryCategory];
        const secondaryTags = currentSettings.tags.filter(tag => !Object.keys(CATEGORIES).includes(tag));

        const index = secondaryTags.indexOf(subCategory);
        if (index > -1) {
            secondaryTags.splice(index, 1);
        } else {
            secondaryTags.push(subCategory);
        }

        setCurrentSettings(prev => ({ ...prev, tags: [primaryCategory, ...secondaryTags] }));
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

    const handleWorkDescriptionChange = (e) => {
        setWorkDescription(e.target.value);
    };

    const handleSave = () => {
        if (validateForm()) {
            const updatedSettings = {
                ...currentSettings,
                saleType,
                salePrice: saleType === 'sale' ? salePrice : '',
                auctionDuration: saleType === 'auction' ? auctionDuration : '',
                startBidPrice: saleType === 'auction' ? startBidPrice : '',
                buyNowPrice: saleType === 'auction' ? buyNowPrice : '',
                workDescription
            };
            onSave(updatedSettings);
        }
    };

    // 2차 카테고리가 선택되었는지 확인
    const hasSecondaryCategory = currentSettings.tags.some(tag =>
        primaryCategory && CATEGORIES[primaryCategory]?.includes(tag)
    );

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
                                    placeholder="작품에 대한 간단한 설명을 입력해주세요. (200자 이내)"
                                    maxLength="200"
                                    className="form-textarea"
                                />
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

                            {currentSettings.tags && currentSettings.tags.length > 1 && (
                                <div className="form-group">
                                    <label>판매 방식</label>
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