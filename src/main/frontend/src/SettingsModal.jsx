import React, { useState, useEffect } from 'react';
import './SettingsModal.css';

const CATEGORIES = {
    '아트워크': ['포토그라피', '일러스트레이션', '스케치', '코믹스'],
    '그래픽디자인': ['타이포그라피', '뮤직패키징', '로고', '그래픽디자인스', '편집'],
    '캐릭터': ['카툰', '애니메', '팬아트', '3D'],
    'Java': ['통신', '알고리즘', 'Thread', 'etc'],
    '프론트엔드': ['HTML', 'CSS', 'Javascript', 'etc'],
    'Python': ['통신', '알고리즘', 'Thread', 'etc'],
};

const SettingsModal = ({ onClose, settings, onSave }) => {
    const [currentSettings, setCurrentSettings] = useState(settings);
    const [primaryCategory, setPrimaryCategory] = useState('');

    useEffect(() => {
        const foundCategory = Object.keys(CATEGORIES).find(cat => currentSettings.tags.includes(cat));
        if (foundCategory) {
            setPrimaryCategory(foundCategory);
        }
    }, [currentSettings.tags]);

    const handleTitleChange = (e) => {
        setCurrentSettings(prev => ({ ...prev, title: e.target.value }));
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
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="settings-modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="settings-modal-header">
                    <h2>프로젝트 설정</h2>
                </div>
                <div className="settings-modal-body">
                    <div className="settings-layout">
                        <div className="settings-left">
                            <div className="cover-image-section">
                                <label>프로젝트 표지</label>
                                <div className="cover-image-container">
                                    {currentSettings.coverImage ? (
                                        <img src={currentSettings.coverImage} alt="프로젝트 표지" />
                                    ) : (
                                        <div className="cover-image-placeholder">
                                            <span>표지 이미지를 선택해주세요</span>
                                        </div>
                                    )}
                                    <input
                                        type="file"
                                        id="cover-image"
                                        accept="image/*"
                                        onChange={handleCoverImageChange}
                                        className="cover-image-input"
                                    />
                                    <label htmlFor="cover-image" className="cover-image-button">
                                        이미지 {currentSettings.coverImage ? '변경' : '선택'}
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div className="settings-right">
                            <div className="settings-section">
                                <label htmlFor="project-title">프로젝트 제목 (필수)</label>
                                <input
                                    type="text"
                                    id="project-title"
                                    value={currentSettings.title}
                                    onChange={handleTitleChange}
                                    placeholder="프로젝트의 제목을 입력하세요"
                                />
                            </div>

                            <div className="settings-section">
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
                                {primaryCategory && (
                                    <div className="secondary-categories-container">
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
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
                <div className="settings-modal-footer">
                    <button className="cancel-button" onClick={onClose}>취소</button>
                    <button className="save-button" onClick={() => onSave(currentSettings)}>프로젝트 업데이트</button>
                </div>
            </div>
        </div>
    );
};

export default SettingsModal; 