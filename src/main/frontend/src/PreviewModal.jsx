import React, { useEffect, useRef } from 'react';
import { X } from 'lucide-react';
import './ResultPage.css';
import './PaywallComponent.css';

// Google Fonts 목록
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

// Google Fonts 로딩 함수
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

const PreviewModal = ({ isOpen, onClose, editorContent, styles }) => {
    const contentRef = useRef(null);

    useEffect(() => {
        if (isOpen && contentRef.current) {
            // Google Fonts 로딩 (미리보기 모달에서 폰트 불러오기)
            if (styles && styles.fontFamily) {
                const selectedFont = GOOGLE_FONTS.find(f => f.family === styles.fontFamily);
                if (selectedFont) {
                    console.log('Loading Google Font for preview modal:', selectedFont.name);
                    loadGoogleFont(selectedFont.name);
                }
            }

            // Photo Grid 요소들을 찾아서 이미지 렌더링
            const photoGrids = contentRef.current.querySelectorAll('[data-type="photo-grid"]');

            photoGrids.forEach(gridElement => {
                try {
                    const layout = JSON.parse(gridElement.getAttribute('data-layout') || '[]');
                    const items = JSON.parse(gridElement.getAttribute('data-items') || '[]');

                    if (items.length > 0) {
                        const maxX = Math.max(...layout.map(l => l.x + l.w), 4);
                        const maxY = Math.max(...layout.map(l => l.y + l.h), 4);

                        gridElement.style.position = 'relative';
                        gridElement.style.width = `${maxX * 50}px`;
                        gridElement.style.height = `${maxY * 50}px`;
                        gridElement.style.margin = '1rem auto';

                        // 기존 내용 지우기
                        gridElement.innerHTML = '';

                        // 이미지들 추가
                        items.forEach((item, index) => {
                            const layoutItem = layout.find(l => l.i === item.id) || { x: 0, y: 0, w: 2, h: 2 };

                            const itemDiv = document.createElement('div');
                            itemDiv.className = 'preview-grid-item';
                            itemDiv.style.cssText = `
                                position: absolute;
                                left: ${layoutItem.x * 50}px;
                                top: ${layoutItem.y * 50}px;
                                width: ${layoutItem.w * 50}px;
                                height: ${layoutItem.h * 50}px;
                                border-radius: 8px;
                                overflow: hidden;
                                box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
                            `;

                            const img = document.createElement('img');
                            img.src = item.src;
                            img.alt = `Grid image ${index + 1}`;
                            img.style.cssText = 'width: 100%; height: 100%; object-fit: cover;';

                            itemDiv.appendChild(img);
                            gridElement.appendChild(itemDiv);
                        });
                    }
                } catch (error) {
                    console.warn('Error rendering photo grid in preview:', error);
                }
            });
        }
    }, [isOpen, editorContent]);

    if (!isOpen) return null;

    const handleBackdropClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    const handleContentClick = (e) => {
        e.stopPropagation();
    };

    return (
        <div className="preview-modal-overlay" onClick={handleBackdropClick}>
            <div className="preview-modal-content" onClick={handleContentClick}>
                <div className="preview-modal-header">
                    <h2>미리보기</h2>
                    <button className="preview-close-button" onClick={onClose}>
                        <X size={20} />
                    </button>
                </div>

                <div className="preview-modal-body" style={styles}>
                    <div
                        ref={contentRef}
                        className="preview-content"
                        dangerouslySetInnerHTML={{ __html: editorContent }}
                    />
                </div>

                <div className="preview-modal-footer" onClick={onClose}>
                    <p>클릭하여 닫기</p>
                </div>
            </div>
        </div>
    );
};

export default PreviewModal; 