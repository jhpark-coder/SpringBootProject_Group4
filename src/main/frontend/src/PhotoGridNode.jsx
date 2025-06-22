import { Node, mergeAttributes } from '@tiptap/core';
import { ReactNodeViewRenderer } from '@tiptap/react';
import PhotoGridComponent from './PhotoGridComponent';

export default Node.create({
    name: 'photoGrid',
    group: 'block',
    atom: true,
    draggable: true,

    addAttributes() {
        return {
            // react-grid-layout's layout array: {i, x, y, w, h}
            layout: { default: [] },
            // Our items array: {id, src}
            items: { default: [] },
        };
    },

    parseHTML() {
        return [{ tag: 'div[data-type="photo-grid"]' }];
    },

    renderHTML({ HTMLAttributes, node }) {
        const { layout = [], items = [] } = node.attrs;

        // 간단한 방식: 데이터만 저장하고 CSS로 처리
        return ['div', mergeAttributes(HTMLAttributes, {
            'data-type': 'photo-grid',
            'data-layout': JSON.stringify(layout),
            'data-items': JSON.stringify(items)
        })];
    },

    addNodeView() {
        return ReactNodeViewRenderer(PhotoGridComponent);
    },

    addCommands() {
        return {
            setPhotoGrid: (attrs) => ({ commands }) => {
                return commands.insertContent({
                    type: this.name,
                    attrs,
                });
            },
        };
    },
}); 