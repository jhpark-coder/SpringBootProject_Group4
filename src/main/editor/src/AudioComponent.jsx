import React from 'react'
import { NodeViewWrapper } from '@tiptap/react'
import { GripVertical } from 'lucide-react'

const AudioComponent = ({ node }) => {
    const { src } = node.attrs

    return (
        <NodeViewWrapper
            className="content-item-wrapper"
            contentEditable={false}
            suppressContentEditableWarning={true}
        >
            <div className="drag-handle" contentEditable={false} data-drag-handle>
                <GripVertical size={18} />
            </div>
            <div className="audio-wrapper">
                <audio src={src} controls={true} contentEditable={false}></audio>
            </div>
        </NodeViewWrapper>
    )
}

export default AudioComponent 