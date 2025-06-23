import React from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import { GripVertical } from 'lucide-react';
import './PaywallComponent.css';

/**
 * 'paywall' 노드가 에디터에 실제로 어떻게 보일지를 정의하는 React 컴포넌트입니다.
 * Tiptap의 NodeView 기능을 사용하여 렌더링됩니다.
 * @param {object} props - Tiptap으로부터 전달받는 속성들
 * @param {object} props.editor - Tiptap 에디터 인스턴스
 */
const PaywallComponent = ({ editor }) => {
  //-- JSX 렌더링 --//
  return (
    // NodeViewWrapper는 Tiptap이 노드를 감싸기 위해 제공하는 필수 컴포넌트입니다.
    // 이 컴포넌트가 있어야 Tiptap이 노드의 위치나 상태를 정확히 제어할 수 있습니다.
    <NodeViewWrapper className="paywall-node">
      
      {/* 드래그 핸들: 이 부분을 드래그하여 노드의 위치를 옮길 수 있습니다. */}
      {/* 'data-drag-handle' 속성이 Tiptap에게 이 요소가 핸들임을 알려주는 중요한 역할을 합니다. */}
      <div className="drag-handle" contentEditable="false" data-drag-handle>
        <GripVertical size={18} />
      </div>

      {/* 실제 눈에 보이는 콘텐츠 부분 */}
      <div className="paywall-content-wrapper">
        <div className="paywall-separator">
          <span className="paywall-text">PREVIEW ENDS HERE</span>
        </div>
        <p className="paywall-description">
          Content below this line will only be visible to paying supporters.
        </p>
      </div>

    </NodeViewWrapper>
  );
};

export default PaywallComponent; 