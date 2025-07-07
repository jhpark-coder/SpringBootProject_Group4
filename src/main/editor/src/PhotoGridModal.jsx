import React, { useState, useCallback, useRef } from 'react';
import './PhotoGridModal.css';
import { Button } from '@/components/ui/button';

// 쿠키에서 특정 이름의 값을 읽어오는 헬퍼 함수
const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const PhotoGridModal = ({ onGridCreate, onClose }) => {
    const [images, setImages] = useState([]);
    const [layout, setLayout] = useState('2-cols');
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState('');
    const fileInputRef = React.useRef(null);

    const handleFileChange = async (event) => {
        const files = Array.from(event.target.files);
        if (files.length === 0) return;

        setIsUploading(true);
        setError('');
        const uploadedImages = [];

        for (const file of files) {
            const formData = new FormData();
            formData.append('file', file);

            try {
                const response = await fetch('/editor/api/upload', {
                    method: 'POST',
                    body: formData,
                });

                if (!response.ok) {
                    throw new Error(`Server responded with ${response.status}`);
                }

                const imageUrl = await response.text();
                // 상대 경로를 Spring Boot 서버의 전체 URL로 변환
                const fullImageUrl = `http://localhost:8080${imageUrl}`;
                uploadedImages.push({ src: fullImageUrl, alt: file.name });

            } catch (err) {
                console.error('Error uploading file:', err);
                setError(`Failed to upload ${file.name}. Please try again.`);
                setIsUploading(false);
                return; // Stop on first error
            }
        }

        setImages(prevImages => [...prevImages, ...uploadedImages]);
        setIsUploading(false);
    };

    const handleCreateClick = () => {
        if (images.length === 0) {
            setError('Please upload at least one image.');
            return;
        }

        const gridData = {
            layout: `grid-${layout}`,
            items: images.map(img => ({ src: img.src, alt: img.alt })),
        };

        onGridCreate(gridData);
        onClose();
    };

    const triggerFileSelect = () => fileInputRef.current.click();

    return (
        <div className="modal-overlay fancy-modal-overlay" onClick={onClose}>
            <div className="modal-content fancy-modal-content animate-fadeIn" onClick={e => e.stopPropagation()}>
                <h2 className="fancy-modal-title">포토 그리드 생성</h2>
                <div className="modal-section">
                    <label className="fancy-label">이미지 업로드 (여러장 선택 가능)</label>
                    <div className="fancy-file-upload-group">
                        <input
                            type="file"
                            multiple
                            accept="image/*"
                            onChange={handleFileChange}
                            className="hidden"
                            id="photo-grid-file-upload"
                            ref={fileInputRef}
                        />
                        <label htmlFor="photo-grid-file-upload" className="fancy-embed-btn fancy-file-btn">
                            파일 선택
                        </label>
                        {images.length > 0 && (
                            <span className="fancy-file-name">
                                {images.length}개의 이미지 선택됨
                            </span>
                        )}
                    </div>
                </div>
                <div className="image-preview">
                    {images.map((image, index) => (
                        <div key={index} className="preview-item">
                            <img src={image.src} alt={image.alt} />
                        </div>
                    ))}
                </div>
                <div className="form-group">
                    <label>Layout</label>
                    <select value={layout} onChange={(e) => setLayout(e.target.value)}>
                        <option value="2-cols">2 Columns</option>
                        <option value="3-cols">3 Columns</option>
                        <option value="4-cols">4 Columns</option>
                        <option value="1-2-cols">1-2 Columns</option>
                        <option value="2-1-cols">2-1 Columns</option>
                    </select>
                </div>
                <div className="flex justify-end space-x-2">
                    <button
                        onClick={onClose}
                        className="fancy-close-btn"
                    >
                        취소
                    </button>
                    <button
                        onClick={handleCreateClick}
                        className="fancy-embed-btn"
                        disabled={isUploading || images.length === 0}
                    >
                        {isUploading ? '업로드 중...' : `그리드 생성 (${images.length})`}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PhotoGridModal; 