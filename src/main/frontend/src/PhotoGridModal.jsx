import React, { useState, useCallback } from 'react';
import './PhotoGridModal.css';

const PhotoGridModal = ({ onGridCreate, onClose }) => {
    const [images, setImages] = useState([]);
    const [layout, setLayout] = useState('2-cols');
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState('');

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
                uploadedImages.push({ src: imageUrl, alt: file.name });

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

    return (
        <div className="modal-overlay">
            <div className="modal-content photo-grid-modal">
                <h2>Create Photo Grid</h2>
                
                <div className="form-group">
                    <label>Upload Images</label>
                    <input type="file" multiple onChange={handleFileChange} accept="image/*" />
                    {isUploading && <p>Uploading...</p>}
                    {error && <p className="error-message">{error}</p>}
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
                
                <div className="modal-actions">
                    <button onClick={handleCreateClick} disabled={isUploading || images.length === 0}>
                        Create Grid
                    </button>
                    <button onClick={onClose} className="cancel-btn">Cancel</button>
                </div>
            </div>
        </div>
    );
};

export default PhotoGridModal; 