import { Node, mergeAttributes } from '@tiptap/core'
import { ReactNodeViewRenderer } from '@tiptap/react'
import IframeComponent from './IframeComponent'

export default Node.create({
    name: 'iframe',
    group: 'block',
    atom: true,
    draggable: true,

    addAttributes() {
        return {
            src: {
                default: null,
            },
            width: {
                default: '100%',
            },
            textAlign: {
                default: 'center',
            },
            frameborder: {
                default: 0,
            },
            allowfullscreen: {
                default: true,
            },
            allow: {
                default: 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share',
            },
            referrerpolicy: {
                default: 'strict-origin-when-cross-origin',
            },
        }
    },

    parseHTML() {
        return [
            {
                tag: 'iframe[src]',
            },
        ]
    },

    renderHTML({ HTMLAttributes }) {
        // `width` and `textAlign` are for the wrapper. The rest are for the iframe.
        const { width, textAlign, ...iframeAttrs } = HTMLAttributes;

        const wrapperStyles = [`width: ${width || '100%'}`]; // Default to 100%
        if (textAlign === 'center') {
            wrapperStyles.push('margin-left: auto', 'margin-right: auto');
        } else if (textAlign === 'right') {
            wrapperStyles.push('margin-left: auto', 'margin-right: 0');
        } else { // 'left' is default
            wrapperStyles.push('margin-left: 0', 'margin-right: auto');
        }

        return [
            'div',
            {
                class: 'iframe-wrapper',
                style: wrapperStyles.join('; '),
            },
            ['iframe', iframeAttrs],
        ];
    },

    addNodeView() {
        return ReactNodeViewRenderer(IframeComponent)
    },

    addCommands() {
        return {
            setIframe: (options) => ({ commands }) => {
                return commands.insertContent({
                    type: this.name,
                    attrs: options,
                });
            },
            setIframeAlignment: (alignment) => ({ commands }) => {
                return commands.updateAttributes(this.name, { textAlign: alignment });
            },
        }
    },
}) 