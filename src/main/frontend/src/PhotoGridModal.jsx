import React, { useState } from 'react';
import { UploadCloud } from 'lucide-react';
import './App.css';

const PREDEFINED_LAYOUTS = {
    '2-Column': (items) => items.map((_, i) => ({
        i: i.toString(),
        x: (i % 2) * 6, // 12칸 그리드의 절반(6) 사용
        y: Math.floor(i / 2) * 8,
        w: 6, // 너비
        h: 8, // 높이 (240px)
    })),
    '3-Column': (items) => items.map((_, i) => ({
        i: i.toString(),
        x: (i % 3) * 4, // 12칸 그리드의 1/3(4) 사용
        y: Math.floor(i / 3) * 6,
        w: 4, // 너비
        h: 6, // 높이 (180px)
    })),
    'Collage-1': (items) => {
        if (items.length < 3) return PREDEFINED_LAYOUTS['2-Column'](items);
        const layouts = [
            { i: '0', x: 0, y: 0, w: 8, h: 12 }, // Main image
            { i: '1', x: 8, y: 0, w: 4, h: 6 },  // Top-right
            { i: '2', x: 8, y: 6, w: 4, h: 6 },  // Bottom-right
        ];
        // Layout for additional images
        items.slice(3).forEach((_, i) => {
            layouts.push({
                i: (i + 3).toString(),
                x: ((i % 3) * 4),
                y: Math.floor(i / 3) * 6 + 12,
                w: 4,
                h: 6,
            });
        });
        return layouts;
    },
};

const PhotoGridModal = ({ onClose, onGridCreate }) => {
    const [selectedLayout, setSelectedLayout] = useState('2-Column');
    const [images, setImages] = useState([]);

    const handleImageChange = async (e) => {
        if (e.target.files) {
            const files = Array.from(e.target.files);
            const uploadedImages = [];

            for (const file of files) {
                try {
                    // 각 파일을 서버에 업로드
                    const formData = new FormData();
                    formData.append('file', file);

                    const response = await fetch('/editor/api/upload', {
                        method: 'POST',
                        body: formData
                    });

                    if (response.ok) {
                        const uploadedUrl = await response.text();
                        uploadedImages.push({
                            id: uploadedUrl, // 업로드된 URL을 ID로 사용
                            src: uploadedUrl
                        });
                    } else {
                        alert(`파일 ${file.name} 업로드에 실패했습니다.`);
                    }
                } catch (error) {
                    console.error('업로드 오류:', error);
                    alert(`파일 ${file.name} 업로드 중 오류가 발생했습니다.`);
                }
            }

            setImages(prevImages => [...prevImages, ...uploadedImages]);
        }
    };

    const removeImage = (id) => {
        setImages(images.filter(image => image.id !== id));
    };

    const handleCreateClick = () => {
        if (images.length === 0) {
            alert("Please upload at least one image.");
            return;
        }
        const layout = PREDEFINED_LAYOUTS[selectedLayout](images);
        const items = images.map((image, i) => ({ id: i.toString(), src: image.src }));

        onGridCreate({ layout, items });
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content photo-grid-modal">
                <h3>Create Photo Grid</h3>
                <div className="layout-options">
                    <label>Layout Style: </label>
                    <select value={selectedLayout} onChange={e => setSelectedLayout(e.target.value)}>
                        {Object.keys(PREDEFINED_LAYOUTS).map(key => (
                            <option key={key} value={key}>{key}</option>
                        ))}
                    </select>
                </div>

                <div className="image-upload-area">
                    <input type="file" id="photo-grid-upload" multiple accept="image/*" onChange={handleImageChange} style={{ display: 'none' }} />
                    <label htmlFor="photo-grid-upload" className="upload-box">
                        <UploadCloud size={32} />
                        <span>Click or Drag to Upload Images</span>
                    </label>
                    <div className="image-preview-list">
                        {images.map(image => (
                            <div key={image.id} className="preview-item">
                                <img src={image.src} alt="preview" />
                                <button onClick={() => removeImage(image.id)}>&times;</button>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="modal-actions">
                    <button onClick={onClose} className="cancel-button">Cancel</button>
                    <button onClick={handleCreateClick} className="create-button">Create Grid</button>
                </div>
            </div>
        </div>
    );
};

export default PhotoGridModal; 