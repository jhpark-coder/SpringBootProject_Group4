import React, { useCallback } from 'react';
import { BubbleMenu } from '@tiptap/react';
import {
    Bold,
    Italic,
    Underline as UnderlineIcon,
    Strikethrough,
    Link as LinkIcon,
    AlignLeft,
    AlignCenter,
    AlignRight,
} from 'lucide-react';

const BubbleMenuComponent = ({ editor }) => {
    // 현재 선택된 텍스트의 폰트 크기를 실시간으로 가져오기
    const getCurrentFontSize = () => {
        if (!editor) return '';

        const { selection } = editor.state;
        const { from, to, empty } = selection;

        // 선택 영역이 없으면 빈 값 반환
        if (empty) return '';

        let fontSize = '';

        // 선택된 영역의 노드들을 순회하며 폰트 크기 찾기
        editor.state.doc.nodesBetween(from, to, (node) => {
            if (node.marks && node.marks.length > 0) {
                const textStyleMark = node.marks.find(mark => mark.type.name === 'textStyle');
                if (textStyleMark && textStyleMark.attrs && textStyleMark.attrs.fontSize) {
                    fontSize = textStyleMark.attrs.fontSize.replace('px', '');
                    return false; // 첫 번째 폰트 크기를 찾으면 중단
                }
            }
        });

        return fontSize;
    };

    const setLink = useCallback(() => {
        if (!editor) return;
        const previousUrl = editor.getAttributes('link').href;
        const url = window.prompt('URL', previousUrl);

        if (url === null) {
            return;
        }
        if (url === '') {
            editor.chain().focus().extendMarkRange('link').unsetLink().run();
            return;
        }
        editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
    }, [editor]);

    // 폰트 크기 변경 핸들러
    const handleFontSizeChange = useCallback((e) => {
        const value = e.target.value;

        // 이벤트 전파 막기
        e.stopPropagation();

        if (value && !isNaN(parseInt(value))) {
            editor.chain().setMark('textStyle', { fontSize: `${value}px` }).run();
        } else if (value === '') {
            editor.chain().unsetMark('textStyle').run();
        }
    }, [editor]);

    if (!editor) {
        return null;
    }

    return (
        <BubbleMenu
            editor={editor}
            tippyOptions={{ duration: 100 }}
            className="bubble-menu"
            shouldShow={({ editor, view, state, oldState, from, to }) => {
                const mediaNodeTypes = ['image', 'video', 'iframe', 'audio', 'photoGrid'];
                const { selection } = state;
                const { $from, $to } = selection;
                const isMediaNode = mediaNodeTypes.some(type =>
                    editor.isActive(type) ||
                    (selection.node && selection.node.type.name === type) ||
                    ($from.parent.type.name === type && $from.pos === $to.pos)
                );
                if (isMediaNode) return false;
                return from !== to;
            }}
        >
            <div className="bubble-menu-group">
                <button onClick={() => editor.chain().focus().toggleBold().run()} className={editor.isActive('bold') ? 'is-active' : ''}> <Bold size={16} /> </button>
                <button onClick={() => editor.chain().focus().toggleItalic().run()} className={editor.isActive('italic') ? 'is-active' : ''}> <Italic size={16} /> </button>
                <button onClick={() => editor.chain().focus().toggleUnderline().run()} className={editor.isActive('underline') ? 'is-active' : ''}> <UnderlineIcon size={16} /> </button>
                <button onClick={() => editor.chain().focus().toggleStrike().run()} className={editor.isActive('strike') ? 'is-active' : ''}> <Strikethrough size={16} /> </button>
                <button onClick={setLink} className={editor.isActive('link') ? 'is-active' : ''}> <LinkIcon size={16} /> </button>
            </div>
            <div className="divider"></div>

            <div className="bubble-menu-group">
                <button
                    onMouseDown={(e) => {
                        e.preventDefault();

                        const current = parseInt(getCurrentFontSize() || '16');
                        const newSize = Math.max(8, current - 1);

                        // 커스텀 FontSize 명령어 사용
                        editor.chain()
                            .focus()
                            .setFontSize(`${newSize}px`)
                            .run();
                    }}
                >-</button>
                <span style={{ color: 'white', padding: '0 8px', minWidth: '30px', textAlign: 'center' }}>
                    {getCurrentFontSize() || '16'}
                </span>
                <button
                    onMouseDown={(e) => {
                        e.preventDefault();

                        const current = parseInt(getCurrentFontSize() || '16');
                        const newSize = Math.min(120, current + 1);

                        // 커스텀 FontSize 명령어 사용
                        editor.chain()
                            .focus()
                            .setFontSize(`${newSize}px`)
                            .run();
                    }}
                >+</button>
            </div>
            <div className="divider"></div>

            <div className="bubble-menu-group">
                <button onClick={() => editor.chain().focus().setTextAlign('left').run()} className={editor.isActive({ textAlign: 'left' }) ? 'is-active' : ''}> <AlignLeft size={16} /> </button>
                <button onClick={() => editor.chain().focus().setTextAlign('center').run()} className={editor.isActive({ textAlign: 'center' }) ? 'is-active' : ''}> <AlignCenter size={16} /> </button>
                <button onClick={() => editor.chain().focus().setTextAlign('right').run()} className={editor.isActive({ textAlign: 'right' }) ? 'is-active' : ''}> <AlignRight size={16} /> </button>
            </div>
        </BubbleMenu>
    );
};

export default BubbleMenuComponent; 