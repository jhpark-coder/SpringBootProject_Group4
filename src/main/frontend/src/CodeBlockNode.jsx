/**
 * @file CodeBlockNode.jsx
 * @description CodeBlockLowlight를 확장하여 커스텀 코드 블록 노드를 정의합니다.
 *              이 노드는 드래그 핸들, 언어 선택, 캡션 등 추가적인 기능을 가집니다.
 *
 * @warning 이 파일을 사용하기 전에 다음 패키지를 설치해야 합니다:
 * npm install lowlight @tiptap/extension-code-block-lowlight highlight.js
 */

import { CodeBlockLowlight } from '@tiptap/extension-code-block-lowlight';
import { ReactNodeViewRenderer } from '@tiptap/react';
import CodeBlockComponent from './CodeBlockComponent.jsx';

const CustomCodeBlock = CodeBlockLowlight.extend({
  name: 'codeBlockNode',

  group: 'block',

  content: 'text*',

  code: true,
  defining: true,

  draggable: true,

  addAttributes() {
    return {
      // 기본 CodeBlockLowlight의 language 속성을 상속받습니다.
      ...this.parent?.(),
      // 캡션 속성을 추가합니다.
      caption: {
        default: '',
        parseHTML: element => element.querySelector('figcaption')?.innerText,
        renderHTML: attributes => {
          if (!attributes.caption) {
            return {};
          }
          return { 'data-caption': attributes.caption };
        },
      },
    };
  },

  addCommands() {
    return {
      insertCodeBlock: (attributes) => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: attributes,
        });
      },
    };
  },

  // Tab 키의 기본 동작을 재정의하여 들여쓰기를 추가합니다.
  addKeyboardShortcuts() {
    return {
      ...this.parent?.(), // 부모(CodeBlockLowlight)의 단축키를 상속받습니다.
      Tab: () => {
        // Tab 키를 눌렀을 때, 2칸의 공백을 삽입하는 명령을 실행합니다.
        // true를 반환하여 브라우저의 기본 동작(포커스 이동)을 막습니다.
        return this.editor.commands.insertContent('  ');
      },
      // Enter 키에 대한 자동 들여쓰기 기능을 추가합니다.
      Enter: () => {
        const { state, commands } = this.editor;
        const { selection } = state;
        const { $from } = selection;

        // 현재 줄의 들여쓰기를 찾습니다.
        const parent = $from.parent;
        const lineTextBeforeCursor = parent.textContent.slice(0, $from.parentOffset)
            .split('\n')
            .pop() || '';
        
        const indentationMatch = lineTextBeforeCursor.match(/^\s*/);
        const indentation = indentationMatch ? indentationMatch[0] : '';

        // 새 줄 문자와 함께 계산된 들여쓰기를 삽입합니다.
        return commands.insertContent('\n' + indentation);
      },
    };
  },

  addNodeView() {
    return ReactNodeViewRenderer(CodeBlockComponent);
  },
});

export default CustomCodeBlock; 