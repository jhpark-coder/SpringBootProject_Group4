import { useState } from 'react';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import CustomImage from './CustomImage.jsx';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import TextAlign from '@tiptap/extension-text-align';
import Iframe from './Iframe.jsx';
import VideoNode from './VideoNode.jsx';
import AudioNode from './AudioNode.jsx';

import Tiptap from './Tiptap.jsx';
import Sidebar from './Sidebar.jsx';
import ImageUploadModal from './ImageUploadModal.jsx';
import StylesModal from './StylesModal.jsx';
import './App.css';

function App() {
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [isStylesModalOpen, setIsStylesModalOpen] = useState(false);
  const [editorStyles, setEditorStyles] = useState({
    backgroundColor: '#ffffff',
    fontFamily: 'sans-serif',
  });

  const editor = useEditor({
    extensions: [
      StarterKit,
      CustomImage,
      Underline,
      Link.configure({ openOnClick: false }),
      TextAlign.configure({
        types: ['heading', 'paragraph', 'iframe', 'audio'],
        addCssClasses: true,
      }),
      Iframe,
      VideoNode,
      AudioNode,
    ],
    content: '<p>Start typing here...</p>',
    editorProps: {
      attributes: {
        class: 'prose-mirror-editor',
      },
    },
  });

  const getYoutubeVideoId = (url) => {
    if (!url) return null;
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
    const match = url.match(regExp);
    if (match && match[2].length === 11) {
      return match[2];
    }
    return null;
  };

  const handleEmbed = (urlOrIframe) => {
    if (!urlOrIframe || !editor) return;

    let urlToProcess = urlOrIframe;

    if (urlOrIframe.trim().startsWith('<iframe')) {
      const srcMatch = urlOrIframe.match(/src="([^"]+)"/);
      if (srcMatch && srcMatch[1]) {
        urlToProcess = srcMatch[1];
      } else {
        return;
      }
    }

    let finalUrl = urlToProcess;

    // If it's NOT already an embed link, try to convert it.
    // Otherwise, use the embed link as is, preserving all parameters.
    if (!urlToProcess.includes('youtube.com/embed/')) {
      const youtubeVideoId = getYoutubeVideoId(urlToProcess);
      if (youtubeVideoId) {
        finalUrl = `https://www.youtube.com/embed/${youtubeVideoId}`;
      }
    }

    editor.chain().focus().setIframe({ src: finalUrl }).run();
  };

  const handleImageAdd = ({ src, alt }) => {
    if (src && editor) {
      editor.chain().focus().setImage({ src, alt }).run();
    }
    setIsImageModalOpen(false);
  };

  return (
    <div className="app-container">
      {isStylesModalOpen && (
        <StylesModal
          onClose={() => setIsStylesModalOpen(false)}
          currentStyles={editorStyles}
          onStyleChange={setEditorStyles}
        />
      )}

      {isImageModalOpen && (
        <ImageUploadModal
          onClose={() => setIsImageModalOpen(false)}
          onImageAdd={handleImageAdd}
        />
      )}

      <div className="main-content">
        <div className="editor-container" style={editorStyles}>
          <Tiptap editor={editor} />
        </div>
        <Sidebar
          editor={editor}
          onEmbed={handleEmbed}
          onImageAdd={() => setIsImageModalOpen(true)}
          onStylesClick={() => setIsStylesModalOpen(true)}
        />
      </div>
    </div>
  );
}

export default App;
