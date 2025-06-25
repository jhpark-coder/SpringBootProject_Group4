import React, { useEffect } from 'react';

// 1. Google Fonts 목록 (인기 폰트 위주로 선정)
const GOOGLE_FONTS = [
    // Korean
    { name: 'Noto Sans KR', family: "'Noto Sans KR', sans-serif" },
    { name: 'Nanum Gothic', family: "'Nanum Gothic', sans-serif" },
    { name: 'Black Han Sans', family: "'Black Han Sans', sans-serif" },
    // English
    { name: 'Roboto', family: "'Roboto', sans-serif" },
    { name: 'Montserrat', family: "'Montserrat', sans-serif" },
    { name: 'Playfair Display', family: "'Playfair Display', serif" },
    // Monospace for code
    { name: 'Source Code Pro', family: "'Source Code Pro', monospace" }
];

// 2. 폰트 로더 함수
const loadGoogleFont = (fontName) => {
    const fontId = `google-font-${fontName.replace(/\s/g, '-')}`;
    if (document.getElementById(fontId)) {
        return; // 이미 로드된 폰트는 다시 로드하지 않음
    }
    const link = document.createElement('link');
    link.id = fontId;
    link.href = `https://fonts.googleapis.com/css2?family=${fontName.replace(/\s/g, '+')}:wght@400;700&display=swap`;
    link.rel = 'stylesheet';
    document.head.appendChild(link);
};

const StylesModal = ({ onClose, currentStyles, onStyleChange }) => {

    const handleStyleChange = (styleName, value) => {
        onStyleChange({ ...currentStyles, [styleName]: value });

        // 3. 폰트 변경 시, 해당 Google Font 동적 로딩
        if (styleName === 'fontFamily') {
            const selectedFont = GOOGLE_FONTS.find(f => f.family === value);
            if (selectedFont) {
                loadGoogleFont(selectedFont.name);
            }
        }
    };

    // 4. 모달이 처음 열릴 때 현재 폰트 로드
    useEffect(() => {
        const selectedFont = GOOGLE_FONTS.find(f => f.family === currentStyles.fontFamily);
        if (selectedFont) {
            loadGoogleFont(selectedFont.name);
        }
    }, []);

    // ESC 키로 모달 닫기
    useEffect(() => {
        const handleKeyDown = (event) => {
            if (event.key === 'Escape') {
                onClose();
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [onClose]);

    return (
        <div className="modal-overlay">
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h3>Editor Styles</h3>
                <div className="modal-section">
                    <label htmlFor="bg-color-picker">Background Color</label>
                    <input
                        id="bg-color-picker"
                        type="color"
                        value={currentStyles.backgroundColor || '#ffffff'}
                        onChange={(e) => handleStyleChange('backgroundColor', e.target.value)}
                    />
                </div>
                <div className="modal-section">
                    <label htmlFor="font-family-select">Font</label>
                    <select
                        id="font-family-select"
                        value={currentStyles.fontFamily || 'sans-serif'}
                        onChange={(e) => handleStyleChange('fontFamily', e.target.value)}
                    >
                        {/* 기본 폰트 옵션 */}
                        <option value="sans-serif">System Sans-serif</option>
                        <option value="serif">System Serif</option>
                        <option value="monospace">System Monospace</option>
                        <optgroup label="Google Fonts">
                            {GOOGLE_FONTS.map(font => (
                                <option key={font.name} value={font.family} style={{ fontFamily: font.family }}>
                                    {font.name}
                                </option>
                            ))}
                        </optgroup>
                    </select>
                </div>
                <div className="modal-footer">
                    <button className="close-button" onClick={onClose}>완료</button>
                    <small style={{ color: '#666', marginTop: '8px', display: 'block' }}>
                        ESC 키를 누르거나 완료 버튼을 클릭하여 닫기
                    </small>
                </div>
            </div>
        </div>
    );
};

export default StylesModal; 