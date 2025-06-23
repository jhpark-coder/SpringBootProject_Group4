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
            items: {
                default: [],
            },
            layout: {
                default: '2-cols',
            },
        };
    },

    parseHTML() {
        return [
            {
                tag: 'div[data-type="photo-grid"]',
                getAttrs: dom => {
                    const items = Array.from(dom.querySelectorAll('.grid-item img')).map(img => ({
                        src: img.getAttribute('src'),
                        alt: img.getAttribute('alt'),
                    }));
                    const layout = dom.className.split(' ').find(cls => cls.includes('-cols')) || '2-cols';
                    return { items, layout };
                }
            },
        ];
    },

    renderHTML({ node, HTMLAttributes }) {
        const { items = [], layout } = node.attrs;

        let layoutClass = 'grid-2-cols'; // Default class
        if (typeof layout === 'string') {
            layoutClass = layout.startsWith('grid-') ? layout : `grid-${layout}`;
        }

        return [
            'div',
            mergeAttributes(HTMLAttributes, { 'data-type': 'photo-grid', class: `photo-grid-wrapper ${layoutClass}` }),
            ...items.map(item => [
                'div',
                { class: 'grid-item' },
                ['img', { src: item.src, alt: item.alt }],
            ]),
        ];
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