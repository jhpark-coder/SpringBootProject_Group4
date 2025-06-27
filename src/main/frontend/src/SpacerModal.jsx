import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';

const SpacerModal = ({ isOpen, currentHeight, onSave, onClose }) => {
    const [height, setHeight] = useState('2');
    const [unit, setUnit] = useState('rem');

    useEffect(() => {
        if (currentHeight) {
            const value = parseFloat(currentHeight) || 2;
            const newUnit = currentHeight.replace(String(value), '') || 'rem';
            setHeight(value);
            setUnit(newUnit);
        } else {
            setHeight('2');
            setUnit('rem');
        }
    }, [currentHeight, isOpen]);

    if (!isOpen) return null;

    const handleSave = () => {
        onSave(`${height}${unit}`);
    };

    return (
        <div className="modal-overlay fancy-modal-overlay" onClick={onClose}>
            <div className="modal-content fancy-modal-content animate-fadeIn" onClick={e => e.stopPropagation()}>
                <h2 className="fancy-modal-title">스페이서 설정</h2>
                <div className="modal-section">
                    <label className="fancy-label">공백 높이</label>
                    <div className="flex items-center gap-2">
                        <input
                            type="number"
                            value={height}
                            onChange={e => setHeight(e.target.value)}
                            className="fancy-input w-full"
                        />
                        <select
                            value={unit}
                            onChange={e => setUnit(e.target.value)}
                            className="fancy-input"
                        >
                            <option value="px">px</option>
                            <option value="rem">rem</option>
                            <option value="em">em</option>
                            <option value="%">%</option>
                        </select>
                    </div>
                </div>
                <div className="flex justify-end space-x-2">
                    <button
                        onClick={handleSave}
                        className="fancy-embed-btn"
                    >
                        저장
                    </button>
                    <button
                        onClick={onClose}
                        className="fancy-embed-btn"
                    >
                        취소
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SpacerModal; 