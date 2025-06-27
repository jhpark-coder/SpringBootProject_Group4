import React, { useState } from 'react';
import { Button } from '@/components/ui/button';

const EmbedModal = ({ onClose, onEmbed }) => {
    const [url, setUrl] = useState('');

    const handleEmbed = () => {
        if (url) {
            onEmbed(url);
            onClose(); // URL을 전달하고 모달을 닫습니다.
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleEmbed();
        }
    };

    return (
        <div className="modal-overlay fancy-modal-overlay" onClick={onClose}>
            <div className="modal-content fancy-modal-content animate-fadeIn" onClick={(e) => e.stopPropagation()}>
                <h3 className="fancy-modal-title">Embed Media</h3>
                <div className="modal-section">
                    <label htmlFor="embed-url" className="fancy-label">YouTube, Vimeo, or other media URL</label>
                    <div className="url-input-group fancy-url-input-group">
                        <input
                            id="embed-url"
                            type="url"
                            placeholder="https://www.youtube.com/watch?v=..."
                            value={url}
                            onChange={(e) => setUrl(e.target.value)}
                            onKeyDown={handleKeyDown}
                            autoFocus
                            className="fancy-input"
                        />
                        <Button onClick={handleEmbed} variant="default" size="lg" className="fancy-embed-btn">Embed</Button>
                    </div>
                </div>
                <Button className="close-button fancy-close-btn" variant="outline" size="lg" onClick={onClose}>Close</Button>
            </div>
        </div>
    );
};

export default EmbedModal; 