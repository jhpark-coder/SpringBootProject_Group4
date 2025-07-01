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
            savedLayouts: {
                default: {},
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
                    
                    // 저장된 레이아웃 정보 파싱
                    let savedLayouts = {};
                    try {
                        const layoutData = dom.getAttribute('data-layouts');
                        if (layoutData) {
                            savedLayouts = JSON.parse(layoutData);
                        }
                    } catch (e) {
                        console.warn('Failed to parse saved layouts:', e);
                    }
                    
                    return { items, layout, savedLayouts };
                }
            },
        ];
    },

    renderHTML({ node, HTMLAttributes }) {
        const { items = [], layout, savedLayouts = {} } = node.attrs;

        let layoutClass = 'grid-2-cols'; // Default class
        if (typeof layout === 'string') {
            layoutClass = layout.startsWith('grid-') ? layout : `grid-${layout}`;
        }

        const attributes = mergeAttributes(HTMLAttributes, { 
            'data-type': 'photo-grid', 
            class: `photo-grid-wrapper ${layoutClass}`,
            'data-layouts': JSON.stringify(savedLayouts)
        });

        return [
            'div',
            attributes,
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