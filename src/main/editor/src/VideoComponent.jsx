import React from 'react'
import { NodeViewWrapper } from '@tiptap/react'
import { GripVertical } from 'lucide-react'
import MediaMenu from './MediaMenu.jsx'

const VideoComponent = ({ editor, node, updateAttributes, selected, deleteNode }) => {
    const { src, width, textAlign = 'left' } = node.attrs

    console.log('VideoComponent rendered with attrs:', node.attrs);
    console.log('VideoComponent src:', src);
    console.log('VideoComponent src type:', typeof src);

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
        console.error('Video src type:', typeof src);
        console.error('Video src value:', src);
    };

    const handleVideoLoad = () => {
        console.log('Video loaded successfully:', src);
    };

    // src가 유효하지 않으면 에러 메시지 표시
    if (!src || src === '[]' || src.trim() === '') {
        return (
            <NodeViewWrapper
                className={`content-item-wrapper ${alignmentClass}`}
                contentEditable={false}
                suppressContentEditableWarning={true}
            >
                <div className="drag-handle" contentEditable={false} data-drag-handle>
                    <GripVertical size={18} />
                </div>
                <div className={`video-container ${alignmentClass}`} style={{ width }}>
                    {selected && <MediaMenu editor={editor} node={node} updateAttributes={updateAttributes} deleteNode={deleteNode} />}
                    <div className="video-wrapper">
                        <div style={{
                            border: '2px solid red',
                            padding: '20px',
                            backgroundColor: '#fff0f0',
                            textAlign: 'center'
                        }}>
                            <h4>비디오 로드 오류</h4>
                            <p>비디오 소스가 유효하지 않습니다.</p>
                            <p>src: {src}</p>
                        </div>
                    </div>
                </div>
            </NodeViewWrapper>
        )
    }

    return (
        <NodeViewWrapper
            className={`content-item-wrapper ${alignmentClass}`}
            contentEditable={false}
            suppressContentEditableWarning={true}
        >
            <div className="drag-handle" contentEditable={false} data-drag-handle>
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
                        contentEditable={false}
                    ></video>
                </div>
            </div>
        </NodeViewWrapper>
    )
}

export default VideoComponent 