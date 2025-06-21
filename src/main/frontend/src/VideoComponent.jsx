import React from 'react'
import { NodeViewWrapper } from '@tiptap/react'
import { GripVertical } from 'lucide-react'
import MediaMenu from './MediaMenu.jsx'

const VideoComponent = ({ editor, node, updateAttributes, selected, deleteNode }) => {
    const { src, width, textAlign = 'left' } = node.attrs

    const getAlignmentClass = () => {
        switch (textAlign) {
            case 'center': return 'has-text-align-center'
            case 'right': return 'has-text-align-right'
            default: return 'has-text-align-left'
        }
    }

    const alignmentClass = getAlignmentClass()

    // Add error handling for video
    const handleVideoError = (e) => {
        console.error('Video failed to load:', src, e);
    };

    const handleVideoLoad = () => {
        console.log('Video loaded successfully:', src);
    };

    return (
        <NodeViewWrapper className={`content-item-wrapper ${alignmentClass}`}>
            <div className="drag-handle" contentEditable={false}>
                <GripVertical size={18} />
            </div>
            <div className={`video-container ${alignmentClass}`} style={{ width }}>
                {selected && <MediaMenu editor={editor} node={node} updateAttributes={updateAttributes} deleteNode={deleteNode} />}
                <div className="video-wrapper">
                    <div className="click-interceptor" contentEditable={false} data-drag-handle></div>
                    <video
                        src={src}
                        controls={true}
                        onError={handleVideoError}
                        onLoadedData={handleVideoLoad}
                        preload="metadata"
                    ></video>
                </div>
            </div>
        </NodeViewWrapper>
    )
}

export default VideoComponent 