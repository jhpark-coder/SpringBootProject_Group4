import React, { useState, useRef } from 'react';

const ImageUploadModal = ({ onClose, onImageAdd }) => {
    const [url, setUrl] = useState('');
    const [alt, setAlt] = useState('');
    const fileInputRef = useRef(null);

    const handleAddFromUrl = () => {
        if (url) {
            onImageAdd({ src: url, alt });
        }
    };

    const handleFileChange = (event) => {
        const file = event.target.files?.[0];
        if (file) {
            const src = URL.createObjectURL(file);
            onImageAdd({ src, alt: file.name });
        }
    };

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h3>Add an Image</h3>
                <div className="modal-section">
                    <label htmlFor="image-url">Embed from URL</label>
                    <div className="url-input-group">
                        <input
                            id="image-url"
                            type="url"
                            placeholder="https://example.com/image.jpg"
                            value={url}
                            onChange={(e) => setUrl(e.target.value)}
                        />
                        <button onClick={handleAddFromUrl}>Add</button>
                    </div>
                </div>
                <div className="modal-divider">OR</div>
                <div className="modal-section">
                    <button className="upload-button" onClick={handleUploadClick}>
                        Upload from Computer
                    </button>
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        accept="image/*"
                        style={{ display: 'none' }}
                    />
                </div>
                <button className="close-button" onClick={onClose}>Close</button>
            </div>
        </div>
    );
};

export default ImageUploadModal; 