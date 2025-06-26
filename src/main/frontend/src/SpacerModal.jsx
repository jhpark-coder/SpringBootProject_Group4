import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';

const SpacerModal = ({ isOpen, currentHeight, onSave, onClose }) => {
    const [height, setHeight] = useState('2');
    const [unit, setUnit] = useState('rem');

    useEffect(() => {
        if (currentHeight) {
            const value = parseFloat(currentHeight);
            const newUnit = currentHeight.replace(String(value), '') || 'rem';
            setHeight(value);
            setUnit(newUnit);
        }
    }, [currentHeight]);

    if (!isOpen) return null;

    const handleSave = () => {
        onSave(`${height}${unit}`);
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ width: '300px' }}>
                <h3>Adjust Spacer Height</h3>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '1rem', marginBottom: '1.5rem' }}>
                    <input
                        type="number"
                        value={height}
                        onChange={(e) => setHeight(e.target.value)}
                        className="modal-input"
                    />
                    <select value={unit} onChange={(e) => setUnit(e.target.value)} className="modal-select">
                        <option value="px">px</option>
                        <option value="rem">rem</option>
                        <option value="%">%</option>
                        <option value="vh">vh</option>
                    </select>
                </div>
                <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                    <Button onClick={handleSave}>Save</Button>
                    <Button variant="outline" onClick={onClose}>Cancel</Button>
                </div>
            </div>
        </div>
    );
};

export default SpacerModal; 