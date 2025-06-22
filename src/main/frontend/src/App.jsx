import { useState } from 'react';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import CustomImage from './CustomImage.jsx';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import TextAlign from '@tiptap/extension-text-align';
import TextStyle from '@tiptap/extension-text-style';
import { mergeAttributes, Extension } from '@tiptap/core';
import Iframe from './Iframe.jsx';
import VideoNode from './VideoNode.jsx';
import AudioNode from './AudioNode.jsx';
import PhotoGridNode from './PhotoGridNode.jsx';

import Tiptap from './Tiptap.jsx';
import Sidebar from './Sidebar.jsx';
import ImageUploadModal from './ImageUploadModal.jsx';
import StylesModal from './StylesModal.jsx';
import PhotoGridModal from './PhotoGridModal.jsx';
import PreviewModal from './PreviewModal.jsx';
import './App.css';

// 커스텀 FontSize 확장
const FontSize = Extension.create({
  name: 'fontSize',

  addGlobalAttributes() {
    return [
      {
        types: ['textStyle'],
        attributes: {
          fontSize: {
            default: null,
            parseHTML: element => element.style.fontSize,
            renderHTML: attributes => {
              if (!attributes.fontSize) {
                return {}
              }
              return {
                style: `font-size: ${attributes.fontSize}`,
              }
            },
          },
        },
      },
    ]
  },

  addCommands() {
    return {
      setFontSize: fontSize => ({ chain }) => {
        return chain()
          .setMark('textStyle', { fontSize })
          .run()
      },
      unsetFontSize: () => ({ chain }) => {
        return chain()
          .setMark('textStyle', { fontSize: null })
          .removeEmptyTextStyle()
          .run()
      },
    }
  },
})

function App() {
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [isStylesModalOpen, setIsStylesModalOpen] = useState(false);
  const [isPhotoGridModalOpen, setIsPhotoGridModalOpen] = useState(false);
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);
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
        types: ['heading', 'paragraph', 'image', 'videoPlayer', 'iframe', 'audio'],
        addCssClasses: true,
      }),
      TextStyle.configure({
        HTMLAttributes: {
          class: 'custom-text-style',
        },
      }),
      FontSize,
      Iframe,
      VideoNode,
      AudioNode,
      PhotoGridNode,
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

    if (!urlToProcess.includes('youtube.com/embed/')) {
      const youtubeVideoId = getYoutubeVideoId(urlToProcess);
      if (youtubeVideoId) {
        finalUrl = `https://www.youtube.com/embed/${youtubeVideoId}`;
      }
    } else {
      // If it's already an embed link, clean it up by removing query parameters
      const urlParts = finalUrl.split('?');
      finalUrl = urlParts[0];
    }

    if (finalUrl) {
      editor.chain().focus().setIframe({ src: finalUrl }).run();
    }
  };

  const handleCreateGrid = (gridData) => {
    if (editor) {
      editor.chain().focus().setPhotoGrid(gridData).run();
    }
    setIsPhotoGridModalOpen(false);
  };

  const handleImageAdd = ({ src, alt }) => {
    if (src && editor) {
      editor.chain().focus().setImage({ src, alt }).run();
    }
    setIsImageModalOpen(false);
  };

  const handlePreviewClick = () => {
    setIsPreviewModalOpen(true);
  };

  const getEditorContent = () => {
    return editor ? editor.getHTML() : '';
  };

  // 문서를 JSON 형태로 저장하기 위한 함수
  const getDocumentData = () => {
    if (!editor) return null;

    const json = editor.getJSON(); // Tiptap의 네이티브 JSON
    const html = editor.getHTML(); // HTML 백업용

    // 모듈 분석 (검색/분석용)
    const modules = [];
    let moduleIndex = 0;

    const extractModules = (content) => {
      content.forEach((node, index) => {
        moduleIndex++;

        switch (node.type) {
          case 'paragraph':
            if (node.content && node.content.length > 0) {
              modules.push({
                index: moduleIndex,
                type: 'text',
                preview: node.content.map(c => c.text || '').join('').substring(0, 100),
                data: node
              });
            }
            break;

          case 'image':
            modules.push({
              index: moduleIndex,
              type: 'image',
              preview: node.attrs.alt || 'Image',
              data: node.attrs
            });
            break;

          case 'photoGrid':
            modules.push({
              index: moduleIndex,
              type: 'photoGrid',
              preview: `Photo Grid (${node.attrs.items?.length || 0} images)`,
              data: node.attrs
            });
            break;

          case 'iframe':
            modules.push({
              index: moduleIndex,
              type: 'video',
              preview: 'Embedded Video',
              data: node.attrs
            });
            break;

          default:
            modules.push({
              index: moduleIndex,
              type: node.type,
              preview: JSON.stringify(node).substring(0, 100),
              data: node
            });
        }
      });
    };

    if (json.content) {
      extractModules(json.content);
    }

    return {
      // 완전한 문서 데이터 (복원용)
      document: {
        tiptap_json: json,
        html_backup: html,
        title: "문서 제목", // 나중에 제목 입력 기능 추가
        created_at: new Date().toISOString()
      },
      // 모듈 분석 데이터 (검색/분석용)
      modules: modules
    };
  };

  // 저장된 JSON에서 에디터로 복원하는 함수
  const loadDocumentData = (documentData) => {
    if (editor && documentData.tiptap_json) {
      editor.commands.setContent(documentData.tiptap_json);
    }
  };

  // 디버깅용: 현재 문서 데이터를 콘솔에 출력
  const debugDocumentData = () => {
    const data = getDocumentData();
    console.log('=== 문서 저장 데이터 ===');
    console.log('전체 JSON:', data.document.tiptap_json);
    console.log('모듈 분석:', data.modules);
    console.log('HTML 백업:', data.document.html_backup);
  };

  // 문서 저장 함수
  const handleSaveDocument = async () => {
    if (!editor) return;

    const data = getDocumentData();
    const title = prompt('문서 제목을 입력하세요:', '새 문서');

    if (!title) return;

    const saveRequest = {
      title: title,
      tiptapJson: JSON.stringify(data.document.tiptap_json),
      htmlBackup: data.document.html_backup
    };

    try {
      const response = await fetch('/editor/api/documents', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(saveRequest)
      });

      if (response.ok) {
        const documentId = await response.json();
        // 결과 페이지로 리다이렉트
        window.location.href = `/editor/result/${documentId}`;
      } else {
        alert('저장에 실패했습니다.');
      }
    } catch (error) {
      console.error('저장 중 오류:', error);
      alert('저장 중 오류가 발생했습니다.');
    }
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

      {isPhotoGridModalOpen && (
        <PhotoGridModal
          onClose={() => setIsPhotoGridModalOpen(false)}
          onGridCreate={handleCreateGrid}
        />
      )}

      {isPreviewModalOpen && (
        <PreviewModal
          isOpen={isPreviewModalOpen}
          onClose={() => setIsPreviewModalOpen(false)}
          editorContent={getEditorContent()}
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
          onPhotoGridClick={() => setIsPhotoGridModalOpen(true)}
          onPreviewClick={handlePreviewClick}
          onDebugClick={debugDocumentData}
          onSaveClick={handleSaveDocument}
        />
      </div>
    </div>
  );
}

export default App;
