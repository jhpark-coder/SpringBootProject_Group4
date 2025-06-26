import { Node, mergeAttributes } from '@tiptap/core';
import { ReactNodeViewRenderer } from '@tiptap/react';
import SpacerComponent from './SpacerComponent';

export default Node.create({
    name: 'spacerNode',
    group: 'block',
    atom: true,
    draggable: true,

    addAttributes() {
        // height 속성을 추가하고, 기본값을 '2rem'으로 설정합니다.
        return {
            height: {
                default: '2rem',
            },
        };
    },

    parseHTML() {
        return [
            {
                tag: 'div[data-type="spacer"]',
                // HTML을 파싱할 때 style 속성에서 높이 값을 읽어옵니다.
                getAttrs: (node) => ({
                    height: node.style.height,
                }),
            },
        ];
    },

    renderHTML({ HTMLAttributes }) {
        // 저장될 HTML에는 속성에 저장된 height 값을 style로 직접 적용합니다.
        return ['div', mergeAttributes(HTMLAttributes, { 'data-type': 'spacer', style: `height: ${this.options.height || HTMLAttributes.height};` })];
    },

    addNodeView() {
        // 에디터 내에서 이 노드를 렌더링할 때 사용할 리액트 컴포넌트를 지정합니다.
        return ReactNodeViewRenderer(SpacerComponent);
    },

    addCommands() {
        return {
            setSpacer: (options) => ({ commands }) => {
                // 이 노드를 에디터에 삽입하는 커스텀 명령어를 정의합니다.
                // options 객체로 { height: '...' } 와 같은 속성을 받을 수 있습니다.
                return commands.insertContent({
                    type: this.name,
                    attrs: options,
                });
            },
        };
    },
}); 