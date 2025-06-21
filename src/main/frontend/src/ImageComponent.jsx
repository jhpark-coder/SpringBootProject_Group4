import React from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import { GripVertical } from 'lucide-react';
import MediaMenu from './MediaMenu.jsx';

const ImageComponent = ({ editor, node, updateAttributes, selected, deleteNode }) => {
    const { src, alt, width, textAlign = 'left' } = node.attrs;

    const getAlignmentClass = () => {
        switch (textAlign) {
            case 'center': return 'has-text-align-center';
            case 'right': return 'has-text-align-right';
            default: return 'has-text-align-left';
        }
    };

    const alignmentClass = getAlignmentClass();

    return (
        <NodeViewWrapper className={`content-item-wrapper ${alignmentClass}`}>
            <div className="drag-handle" contentEditable={false}>
                <GripVertical size={18} />
            </div>
            <div className={`image-container ${alignmentClass}`} style={{ width }}>
                {selected && <MediaMenu editor={editor} node={node} updateAttributes={updateAttributes} deleteNode={deleteNode} />}
                <div className="image-wrapper">
                    <div className="click-interceptor" contentEditable={false} data-drag-handle></div>
                    <img src={src} alt={alt} />
                </div>
            </div>
        </NodeViewWrapper>
    );
};

export default ImageComponent; 