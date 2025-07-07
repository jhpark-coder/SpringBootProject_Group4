/**
 * @file CodeBlockComponent.jsx
 * @description 커스텀 코드 블록 노드의 UI를 렌더링하는 React 컴포넌트입니다.
 *              Tiptap의 NodeView 기능을 사용하여 에디터와 상호작용합니다.
 */
import React, { useState } from 'react';
import { NodeViewWrapper, NodeViewContent } from '@tiptap/react';
import { GripVertical, Copy, Trash2, MessageSquareText } from 'lucide-react';
import './CodeBlockComponent.css';

const CodeBlockComponent = ({ node, updateAttributes, deleteNode, getPos, editor }) => {
  const { language, caption } = node.attrs;
  const [isCaptionVisible, setIsCaptionVisible] = useState(!!caption);

  const handleCopy = () => {
    navigator.clipboard.writeText(node.textContent);
    // You might want to show a small notification "Copied!"
  };

  const toggleCaption = () => {
    if (isCaptionVisible && caption) {
      // If caption is visible and has text, don't just hide, clear it.
      // Or decide on a different behavior. For now, we just hide.
      updateAttributes({ caption: '' });
      setIsCaptionVisible(false);
    } else {
      setIsCaptionVisible(!isCaptionVisible);
    }
  };

  // 코드 블록 컨테이너 클릭 시 커서를 블록 밖으로 이동시키는 함수
  const handleContainerClick = (e) => {
    // 코드 영역(pre)이나 다른 버튼들을 클릭한 경우는 무시
    if (e.target.closest('pre') || e.target.closest('button') || e.target.closest('select') || e.target.closest('input')) {
      return;
    }
    
    // 컨테이너의 오른쪽 부분을 클릭한 경우 커서를 코드 블록 다음으로 이동
    const containerRect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX;
    const containerRight = containerRect.right;
    
    // 클릭 위치가 컨테이너 너비의 70% 이상인 경우 (오른쪽 공백 영역)
    if (clickX > containerRect.left + (containerRect.width * 0.7)) {
      const pos = getPos() + node.nodeSize;
      editor.commands.setTextSelection(pos);
    }
  };

  return (
    <NodeViewWrapper className="custom-code-block-wrapper">
      <div className="drag-handle" contentEditable={false} data-drag-handle>
        <GripVertical size={18} />
      </div>

      <div className="code-block-container" onClick={handleContainerClick}>
        <div className="code-block-header">
          <select
            value={language || 'auto'}
            onChange={e => updateAttributes({ language: e.target.value })}
            className="language-selector"
            contentEditable={false}
          >
            <option value="auto">auto</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
            <option value="javascript">JavaScript</option>
            <option value="typescript">TypeScript</option>
            <option value="html">HTML</option>
            <option value="css">CSS</option>
          </select>
          
          <div className="code-block-toolbar">
            <button onClick={handleCopy} title="Copy code">
              <Copy size={16} />
              <span>Copy</span>
            </button>
            <button onClick={toggleCaption} title="Add caption">
              <MessageSquareText size={16} />
              <span>Caption</span>
            </button>
            <button onClick={deleteNode} title="Delete node">
              <Trash2 size={16} />
            </button>
          </div>
        </div>
        
        <pre className="code-block-content" spellCheck="false">
          <NodeViewContent as="code" />
        </pre>

        {isCaptionVisible && (
          <div className="caption-input-wrapper">
            <input
              type="text"
              className="caption-input"
              value={caption}
              onChange={e => updateAttributes({ caption: e.target.value })}
              placeholder="Write a caption..."
            />
          </div>
        )}
      </div>
    </NodeViewWrapper>
  );
};

export default CodeBlockComponent; 