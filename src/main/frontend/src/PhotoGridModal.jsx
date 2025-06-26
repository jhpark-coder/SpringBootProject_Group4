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
        <div className="modal-overlay">
            <div className="modal-content photo-grid-modal">
                <h2>Create Photo Grid</h2>

                <div className="form-group">
                    <label>Upload Images</label>
                    <input
                        type="file"
                        multiple
                        onChange={handleFileChange}
                        accept="image/*"
                        ref={fileInputRef}
                        style={{ display: 'none' }}
                    />
                    <Button variant="outline" onClick={triggerFileSelect}>
                        Choose Files
                    </Button>
                    {isUploading && <p style={{ marginTop: '0.5rem' }}>Uploading...</p>}
                    {error && <p className="error-message" style={{ marginTop: '0.5rem' }}>{error}</p>}
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

                <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                    <Button onClick={handleCreateClick} disabled={isUploading || images.length === 0}>Create Grid</Button>
                    <Button variant="outline" onClick={onClose}>Cancel</Button>
                </div>
            </div>
        </div>
    );
};

export default PhotoGridModal; 