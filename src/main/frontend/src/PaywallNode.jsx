import { Node, mergeAttributes } from '@tiptap/core';
import { ReactNodeViewRenderer } from '@tiptap/react';
import PaywallComponent from './PaywallComponent';

/**
 * Tiptap 에디터에 'paywall'이라는 새로운 콘텐츠 타입(노드)을 정의하는 파일입니다.
 * Tiptap은 이 설정 파일을 읽어서 'paywall'을 어떻게 처리해야 할지 알게 됩니다.
 */
export default Node.create({
  // 노드의 고유한 이름입니다. 에디터 내부에서 이 이름으로 식별됩니다.
  name: 'paywall',

  // 노드의 그룹을 지정합니다. 'block'은 단락처럼 독립된 한 줄을 차지하는 요소를 의미합니다.
  // 'inline'은 텍스트처럼 줄 안에 포함되는 요소를 의미합니다.
  group: 'block',

  // true로 설정하면, 이 노드는 더 이상 쪼개질 수 없는 하나의 단위로 취급됩니다.
  // 사용자가 이 노드의 내부 텍스트를 편집할 수 없게 됩니다.
  atom: true, 
  
  // 이 노드를 드래그해서 위치를 옮길 수 있게 할지 여부를 설정합니다.
  draggable: true,

  /**
   * HTML 데이터를 에디터의 JSON 데이터로 변환하는 규칙을 정의합니다.
   * 예를 들어, 저장된 HTML을 다시 불러올 때 이 규칙이 사용됩니다.
   */
  parseHTML() {
    return [
      {
        // 'div' 태그이면서 'data-type' 속성이 'paywall'인 것을 찾으면,
        // 이 노드로 변환하라는 의미입니다.
        tag: 'div[data-type="paywall"]',
      },
    ];
  },

  /**
   * 에디터의 JSON 데이터를 실제 HTML로 변환하는 규칙을 정의합니다.
   * 문서를 저장할 때나, 최종 결과 페이지를 보여줄 때 이 규칙이 사용됩니다.
   * @param {object} param
   * @param {object} param.HTMLAttributes - 노드에 적용될 HTML 속성들
   */
  renderHTML({ HTMLAttributes }) {
    // 최종적으로 생성될 HTML의 구조를 배열 형태로 정의합니다.
    // ['태그이름', {속성객체}, '자식요소1', '자식요소2', ...]
    return [
      'div',
      // 원래 속성과 우리가 추가할 속성을 합칩니다.
      mergeAttributes(HTMLAttributes, { 'data-type': 'paywall', class: 'paywall-node' }),
      [
        'div',
        { class: 'paywall-separator' },
        ['span', { class: 'paywall-text' }, 'PREVIEW ENDS HERE'],
      ],
      [
        'p',
        { class: 'paywall-description' },
        'Content below this line will only be visible to paying supporters.',
      ],
    ];
  },

  /**
   * 이 노드가 에디터 안에서 보여질 때, 어떤 React 컴포넌트를 사용할지 지정합니다.
   * 이 설정을 통해 Tiptap의 기본 HTML 렌더링 대신, 우리가 만든 React 컴포넌트로 보이게 할 수 있습니다.
   */
  addNodeView() {
    // PaywallComponent.jsx 파일을 렌더링하도록 연결합니다.
    return ReactNodeViewRenderer(PaywallComponent);
  },

  /**
   * 이 노드와 관련된 새로운 명령어(command)를 추가합니다.
   * 이렇게 추가된 명령어는 editor.chain().focus().[명령어이름]().run() 형태로 사용할 수 있습니다.
   */
  addCommands() {
    return {
      // 'setPaywall' 이라는 새로운 명령어를 정의합니다.
      setPaywall: () => ({ commands }) => {
        // 현재 커서 위치에 이 노드('paywall' 타입)를 삽입하는 내장 명령어를 실행합니다.
        return commands.insertContent({
          type: this.name,
        });
      },
    };
  },
}); 