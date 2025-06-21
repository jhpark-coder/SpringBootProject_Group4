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
            textAlign: {
                default: 'left',
            },
        };
    },

    addNodeView() {
        return ReactNodeViewRenderer(ImageComponent);
    },
});

export default CustomImage; 