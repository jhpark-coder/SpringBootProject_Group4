import React from 'react'
import { NodeViewWrapper } from '@tiptap/react'
import { GripVertical } from 'lucide-react'
import MediaMenu from './MediaMenu.jsx'

const IframeComponent = ({ editor, node, updateAttributes, selected, deleteNode }) => {
    const { src, width, textAlign = 'center' } = node.attrs

    const containerStyle = {
        width: width,
        marginTop: '1rem',
        marginBottom: '1rem',
        marginLeft: 'auto',
        marginRight: 'auto'
    }

    if (textAlign === 'left') {
        containerStyle.marginLeft = '0'
    } else if (textAlign === 'right') {
        containerStyle.marginRight = '0'
    }

    if (!src) {
        return (
            <NodeViewWrapper>
                <div style={{ border: '2px solid red', padding: '10px', backgroundColor: '#fff0f0' }}>
                    <h4>Iframe 렌더링 오류: SRC 주소 없음</h4>
                    <p>전달된 속성: {JSON.stringify(node.attrs)}</p>
                </div>
            </NodeViewWrapper>
        )
    }

    return (
        <NodeViewWrapper className="content-item-wrapper">
            <div className="drag-handle" contentEditable={false} data-drag-handle>
                <GripVertical size={18} />
            </div>
            <div className="iframe-container" style={containerStyle}>
                {selected && <MediaMenu editor={editor} node={node} updateAttributes={updateAttributes} deleteNode={deleteNode} />}
                <div className="iframe-wrapper">
                    <div className="click-interceptor" contentEditable={false}></div>
                    <iframe
                        className="iframe-content"
                        src={src}
                        frameBorder="0"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                        allowFullScreen
                    ></iframe>
                </div>
            </div>
        </NodeViewWrapper>
    )
}

export default IframeComponent