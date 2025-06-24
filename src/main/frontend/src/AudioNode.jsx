import { Node, mergeAttributes } from '@tiptap/core'
import { ReactNodeViewRenderer } from '@tiptap/react'
import AudioComponent from './AudioComponent'

export default Node.create({
    name: 'audio',
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
        }
    },

    parseHTML() {
        return [
            {
                tag: 'audio',
            },
        ]
    },

    renderHTML({ HTMLAttributes }) {
        return ['audio', mergeAttributes(HTMLAttributes, { controls: 'true' })]
    },

    addNodeView() {
        return ReactNodeViewRenderer(AudioComponent)
    },

    addCommands() {
        return {
            setAudio: (options) => ({ commands }) => {
                return commands.insertContent({
                    type: this.name,
                    attrs: options,
                });
            },
        }
    },
}) 