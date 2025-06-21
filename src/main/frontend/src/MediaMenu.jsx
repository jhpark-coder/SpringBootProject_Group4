import React from 'react';
import { AlignLeft, AlignCenter, AlignRight, Trash2, RotateCcw } from 'lucide-react';

const MediaMenu = ({ editor, node, updateAttributes, deleteNode }) => {
    const { width, textAlign } = node.attrs;

    const handleAlignment = (align) => {
        updateAttributes({ textAlign: align });
    };

    const handleSize = (newWidth) => {
        updateAttributes({ width: newWidth });
    };

    const resetAlignment = () => {
        updateAttributes({ textAlign: 'left' });
    };

    return (
        <div className="media-menu">
            <button onClick={() => handleSize('100%')} className={width === '100%' ? 'is-active' : ''}>100%</button>
            <button onClick={() => handleSize('75%')} className={width === '75%' ? 'is-active' : ''}>75%</button>
            <button onClick={() => handleSize('50%')} className={width === '50%' ? 'is-active' : ''}>50%</button>
            <button onClick={() => handleSize('25%')} className={width === '25%' ? 'is-active' : ''}>25%</button>
            <button onClick={() => handleSize('100%')} title="Original size"><RotateCcw size={18} /></button>
            <div className="divider"></div>
            <button onClick={() => handleAlignment('left')} className={textAlign === 'left' ? 'is-active' : ''}><AlignLeft size={18} /></button>
            <button onClick={() => handleAlignment('center')} className={textAlign === 'center' ? 'is-active' : ''}><AlignCenter size={18} /></button>
            <button onClick={() => handleAlignment('right')} className={textAlign === 'right' ? 'is-active' : ''}><AlignRight size={18} /></button>
            <button onClick={resetAlignment} title="Reset alignment"><RotateCcw size={18} /></button>
            <div className="divider"></div>
            <button onClick={deleteNode}><Trash2 size={18} /></button>
        </div>
    );
};

export default MediaMenu; 