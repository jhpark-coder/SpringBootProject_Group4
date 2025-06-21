import { EditorContent, BubbleMenu } from '@tiptap/react';
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

const Tiptap = ({ editor }) => {
    const setLink = () => {
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
    };

    if (!editor) {
        return null;
    }

    return (
        <>
            <BubbleMenu
                editor={editor}
                tippyOptions={{ duration: 100 }}
                shouldShow={({ editor, view, state, oldState, from, to }) => {
                    // Don't show the bubble menu for our custom media nodes
                    const mediaNodeTypes = ['image', 'video', 'iframe', 'audio'];
                    const { selection } = state;
                    const { $from, $to } = selection;
                    const isMediaNode = mediaNodeTypes.some(type =>
                        editor.isActive(type) ||
                        (selection.node && selection.node.type.name === type) ||
                        ($from.parent.type.name === type && $from.pos === $to.pos)
                    );

                    if (isMediaNode) {
                        return false;
                    }

                    // Show for text selections
                    return from !== to;
                }}
            >
                <div className="bubble-menu">
                    <button onClick={() => editor.chain().focus().toggleBold().run()} className={editor.isActive('bold') ? 'is-active' : ''}>
                        <Bold size={16} />
                    </button>
                    <button onClick={() => editor.chain().focus().toggleItalic().run()} className={editor.isActive('italic') ? 'is-active' : ''}>
                        <Italic size={16} />
                    </button>
                    <button onClick={() => editor.chain().focus().toggleUnderline().run()} className={editor.isActive('underline') ? 'is-active' : ''}>
                        <UnderlineIcon size={16} />
                    </button>
                    <button onClick={() => editor.chain().focus().toggleStrike().run()} className={editor.isActive('strike') ? 'is-active' : ''}>
                        <Strikethrough size={16} />
                    </button>
                    <button onClick={setLink} className={editor.isActive('link') ? 'is-active' : ''}>
                        <LinkIcon size={16} />
                    </button>
                    <div className="divider"></div>
                    <button
                        onClick={() => editor.chain().focus().setTextAlign('left').run()}
                        className={editor.isActive({ textAlign: 'left' }) ? 'is-active' : ''}
                    >
                        <AlignLeft size={16} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().setTextAlign('center').run()}
                        className={editor.isActive({ textAlign: 'center' }) ? 'is-active' : ''}
                    >
                        <AlignCenter size={16} />
                    </button>
                    <button
                        onClick={() => editor.chain().focus().setTextAlign('right').run()}
                        className={editor.isActive({ textAlign: 'right' }) ? 'is-active' : ''}
                    >
                        <AlignRight size={16} />
                    </button>
                </div>
            </BubbleMenu>
            <EditorContent editor={editor} />
        </>
    );
};

export default Tiptap; 