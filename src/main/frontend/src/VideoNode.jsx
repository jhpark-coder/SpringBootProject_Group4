import { Node, mergeAttributes } from '@tiptap/core'
import { ReactNodeViewRenderer } from '@tiptap/react'
import VideoComponent from './VideoComponent'

export default Node.create({
    name: 'videoPlayer', // Changed name to avoid conflict with potential native 'video'
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
                default: 'left',
            },
        }
    },

    parseHTML() {
        return [
            {
                tag: 'video',
            },
        ]
    },

    renderHTML({ HTMLAttributes }) {
        return ['video', mergeAttributes(HTMLAttributes, { controls: 'true' })]
    },

    addNodeView() {
        return ReactNodeViewRenderer(VideoComponent)
    },

    addCommands() {
        return {
            setVideo: (options) => ({ chain }) => {
                return chain()
                    .insertContent({
                        type: this.name,
                        attrs: options,
                    })
                    .insertContent({
                        type: 'paragraph',
                        content: '',
                    })
                    .focus();
            },
        }
    },
}) 