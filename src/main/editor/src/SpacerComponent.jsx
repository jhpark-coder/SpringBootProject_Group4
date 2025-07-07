import React from 'react';
import { NodeViewWrapper, NodeViewContent } from '@tiptap/react';

const SpacerComponent = ({ node, updateAttributes, editor }) => {
    // 이 컴포넌트는 에디터 내에서 spacer 노드를 렌더링하는 데 사용됩니다.
    // NodeViewWrapper로 감싸야 Tiptap이 노드를 올바르게 제어할 수 있습니다.
    const { height } = node.attrs;

    const openModal = () => {
        // App.jsx에 정의된 함수를 호출하여 모달을 엽니다.
        // editor.storage.custom.openSpacerModal(node, updateAttributes);
        editor.storage.spacer.openModal(
            node.attrs.height,
            (newHeight) => updateAttributes({ height: newHeight })
        );
    };

    return (
        <NodeViewWrapper
            className="spacer-component-wrapper"
            contentEditable={false}
            suppressContentEditableWarning={true}
            onClick={openModal}
            title="Click to adjust height"
            style={{ cursor: 'pointer' }}
        >
            <div
                className="spacer-component"
                contentEditable={false}
                style={{
                    height: height,
                    border: '1px dashed #ccc', // 편집 중 보이기 쉽도록 점선 테두리 추가
                    margin: '1rem 0'
                }}
            ></div>
        </NodeViewWrapper>
    );
};

export default SpacerComponent; 