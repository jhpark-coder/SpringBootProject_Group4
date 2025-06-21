import React from 'react';

const StylesModal = ({ onClose, currentStyles, onStyleChange }) => {

    const handleStyleChange = (styleName, value) => {
        onStyleChange({ ...currentStyles, [styleName]: value });
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
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
                        <option value="serif">Serif (e.g., Times New Roman)</option>
                        <option value="sans-serif">Sans-serif (e.g., Arial)</option>
                        <option value="monospace">Monospace (e.g., Courier)</option>
                    </select>
                </div>
                <button className="close-button" onClick={onClose}>Done</button>
            </div>
        </div>
    );
};

export default StylesModal; 