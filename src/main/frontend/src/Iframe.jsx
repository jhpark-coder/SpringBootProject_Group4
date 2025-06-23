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
                tag: 'iframe',
            },
        ]
    },

    renderHTML({ HTMLAttributes }) {
        return ['iframe', mergeAttributes(HTMLAttributes)]
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