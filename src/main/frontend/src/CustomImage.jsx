//================================================================================
// 0. 모듈 임포트
//================================================================================
import { mergeAttributes } from '@tiptap/core'; // Tiptap의 속성(attribute)들을 병합하는 유틸리티 함수입니다.
import Image from '@tiptap/extension-image'; // Tiptap이 기본으로 제공하는 이미지 확장 기능을 가져옵니다.
import { ReactNodeViewRenderer } from '@tiptap/react'; // Tiptap 노드를 리액트 컴포넌트로 렌더링할 수 있게 해주는 뷰 렌더러입니다.
import ImageComponent from './ImageComponent.jsx'; // 이미지를 실제로 화면에 표시할 리액트 컴포넌트입니다.

//================================================================================
// 1. 커스텀 이미지 확장(Extension) 정의
//================================================================================
// Tiptap의 기본 `Image` 확장 기능을 'extend'(확장)하여 `CustomImage`라는 새로운 확장 기능을 만듭니다.
// 이를 통해 기본 이미지 기능은 그대로 유지하면서, 우리가 원하는 새로운 기능(속성, 뷰 등)을 덧붙일 수 있습니다.
export const CustomImage = Image.extend({

    // `addAttributes` 메서드: 이 커스텀 이미지 노드가 가질 수 있는 HTML 속성을 정의합니다.
    addAttributes() {
        return {
            // `...this.parent?.()`: Tiptap의 기본 Image 확장이 가지고 있던 원래 속성들(src, alt 등)을 그대로 상속받습니다.
            ...this.parent?.(),
            // `width` 속성을 새로 추가합니다. 기본값은 '100%'입니다.
            width: {
                default: '100%',
            },
            // `textAlign` 속성을 추가합니다. 텍스트 정렬을 위해 사용되며, 기본값은 'left'입니다.
            textAlign: {
                default: 'left',
            },
            // `data-float` 속성을 새로 추가합니다. 좌우 정렬(float)을 위해 사용되며, 기본값은 없습니다(null).
            'data-float': {
                default: null,
            },
        };
    },

    // `addNodeView` 메서드: 이 노드가 에디터 안에서 어떻게 보이고 동작할지를 정의합니다.
    // 여기서는 기본 HTML 렌더링 대신, 우리가 만든 `ImageComponent`라는 리액트 컴포넌트를 사용하도록 설정합니다.
    // 이를 통해 이미지와 관련된 복잡한 UI나 상호작용을 리액트 컴포넌트로 쉽게 구현할 수 있습니다.
    addNodeView() {
        return ReactNodeViewRenderer(ImageComponent);
    },

    // `renderHTML` 메서드: 에디터의 콘텐츠를 최종적으로 HTML로 변환할 때(예: 저장 시) 이 노드를 어떻게 렌더링할지 정의합니다.
    // `addNodeView`가 에디터 내부에서의 '보이는 모습'을 담당한다면, `renderHTML`은 최종 결과물(HTML 파일)을 담당합니다.
    renderHTML({ HTMLAttributes }) {
        // 기본 이미지 퓨(HTML) 구조를 가져옵니다.
        const originalHTML = this.parent?.({ HTMLAttributes });
        if (!Array.isArray(originalHTML) || originalHTML[0] !== 'img') {
            return originalHTML;
        }

        // data-float 속성과 textAlign 속성을 HTML에 직접 추가합니다.
        const { 'data-float': float, textAlign } = HTMLAttributes;
        let style = HTMLAttributes.style || '';

        if (float) {
            style = style ? `${style}; float: ${float};` : `float: ${float};`;
        }

        if (textAlign) {
            style = style ? `${style}; text-align: ${textAlign};` : `text-align: ${textAlign};`;
        }

        if (style) {
            HTMLAttributes.style = style;
        }

        //最终的に 'img' 태그를 생성하고, 기본 속성과 커스텀 속성을 모두 병합하여 적용합니다.
        return [
            'img',
            mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
        ];
    },

});

export default CustomImage; 