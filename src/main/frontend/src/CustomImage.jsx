import { mergeAttributes } from '@tiptap/core';
import Image from '@tiptap/extension-image';
import { ReactNodeViewRenderer } from '@tiptap/react';
import ImageComponent from './ImageComponent.jsx';

export const CustomImage = Image.extend({
    
    addAttributes() {
        return {
            ...this.parent?.(),
            width: {
                default: '100%',
            },
            'data-float': {
                default: null,
            },
        };
    },
    
    addNodeView() {
        return ReactNodeViewRenderer(ImageComponent);
    },

    renderHTML({ HTMLAttributes }) {
        // 기본 이미지 렌더링 HTML 구조를 가져옵니다.
        const originalHTML = this.parent?.({ HTMLAttributes });
        if (!Array.isArray(originalHTML) || originalHTML[0] !== 'img') {
          return originalHTML;
        }

        // data-float 속성이 있으면 스타일을 추가합니다.
        const { 'data-float': float } = HTMLAttributes;
        if (float) {
            const style = HTMLAttributes.style ? HTMLAttributes.style + ';' : '';
            HTMLAttributes.style = `${style} float: ${float};`;
        }
    
        return [
            'img',
            mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
        ];
    },

});

export default CustomImage; 