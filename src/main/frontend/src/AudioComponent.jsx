import React from 'react'
import { NodeViewWrapper } from '@tiptap/react'
import { GripVertical } from 'lucide-react'

const AudioComponent = ({ node }) => {
    const { src } = node.attrs

    return (
        <NodeViewWrapper className="content-item-wrapper">
            <div className="drag-handle" contentEditable={false} data-drag-handle>
                <GripVertical size={18} />
            </div>
            <div className="audio-wrapper">
                <audio src={src} controls={true}></audio>
            </div>
        </NodeViewWrapper>
    )
}

export default AudioComponent 