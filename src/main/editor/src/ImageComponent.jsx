import React from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import { GripVertical } from 'lucide-react';
import MediaMenu from './MediaMenu.jsx';

const ImageComponent = ({ node, updateAttributes, editor, deleteNode }) => {
    const { src, alt, width, textAlign } = node.attrs;
    const isSelected = editor.isActive('image', { src });

    // textAlign 값에 따라 적절한 CSS 클래스를 반환하는 함수
    const getAlignmentClass = () => {
        switch (textAlign) {
            case 'center':
                return 'has-text-align-center';
            case 'right':
                return 'has-text-align-right';
            default:
                return 'has-text-align-left';
        }
    };

    return (
        <NodeViewWrapper
            className={`image-container resizable ${getAlignmentClass()}`}
            contentEditable={false}
            suppressContentEditableWarning={true}
            data-drag-handle
        >
            <div className="drag-handle" contentEditable={false} data-drag-handle>
                <GripVertical size={18} />
            </div>

            <img
                src={src}
                alt={alt}
                style={{ width: width }}
                className={`tiptap-image ${isSelected ? 'ProseMirror-selectednode' : ''}`}
                contentEditable={false}
                draggable={false}
            />

            {isSelected && <MediaMenu editor={editor} node={node} updateAttributes={updateAttributes} deleteNode={deleteNode} />}
        </NodeViewWrapper>
    );
};

export default ImageComponent; 