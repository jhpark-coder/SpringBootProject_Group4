/**
 * @file App.jsx
 * @description 이 애플리케이션의 메인 컴포넌트입니다. Tiptap 에디터의 초기화, 상태 관리,
 *              데이터 로딩 및 저장, UI 렌더링 등 에디터의 모든 핵심 로직을 담당합니다.
 */

import { useState, useEffect, useRef } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import { useParams } from 'react-router-dom';
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
import PaywallNode from './PaywallNode.jsx';

import BubbleMenuComponent from './BubbleMenuComponent.jsx';
import Sidebar from './Sidebar.jsx';
import ImageUploadModal from './ImageUploadModal.jsx';
import StylesModal from './StylesModal.jsx';
import PhotoGridModal from './PhotoGridModal.jsx';
import PreviewModal from './PreviewModal.jsx';
import EmbedModal from './EmbedModal.jsx';
import SettingsModal from './SettingsModal.jsx';
import './App.css';

//================================================================================
// Tiptap 확장 기능 직접 만들기 (커스텀 FontSize)
// Tiptap은 이런 식으로 필요한 기능을 직접 만들어 붙일 수 있는 유연한 구조를 가집니다.
//================================================================================
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

//================================================================================
// 메인 애플리케이션 컴포넌트
//================================================================================
function App() {

  //----------------------------------------------------------------
  // 1. 상태 관리 (State Management)
  // React의 useState는 컴포넌트가 기억해야 할 '상태'를 만듭니다.
  // 이 상태값이 바뀌면 화면이 자동으로 다시 렌더링됩니다.
  //----------------------------------------------------------------

  const { id } = useParams(); // URL 파라미터에서 문서 ID를 가져옵니다. (예: /editor/123 -> id는 '123')
  const hasLoaded = useRef(false); // 로딩 잠금장치
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [isEmbedModalOpen, setIsEmbedModalOpen] = useState(false);
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false);
  const [isStylesModalOpen, setIsStylesModalOpen] = useState(false);
  const [isPhotoGridModalOpen, setIsPhotoGridModalOpen] = useState(false);
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);
  const [editorStyles, setEditorStyles] = useState({
    backgroundColor: '#ffffff',
    fontFamily: 'sans-serif',
  });
  const [projectSettings, setProjectSettings] = useState({
    title: '',
    coverImage: '',
    tags: []
  });
  const [initialContent, setInitialContent] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  //----------------------------------------------------------------
  // 2. Tiptap 에디터 초기화
  // `useEditor` 훅(hook)을 사용하여 Tiptap 에디터 인스턴스를 생성합니다.
  //----------------------------------------------------------------
  const editor = useEditor({
    // 에디터에서 사용할 확장 기능들을 배열로 등록합니다. 여기에 등록해야 해당 기능을 쓸 수 있습니다.
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
      PaywallNode,
    ],
    // 에디터의 초기 내용은 항상 비워둡니다. 서버에서 비동기적으로 데이터를 불러온 후 채워넣을 것입니다.
    content: '',
    // 에디터의 HTML 최상위 요소에 적용될 속성입니다. (CSS 클래스 등)
    editorProps: {
      attributes: {
        class: 'prose-mirror-editor',
      },
    },
  });

  //----------------------------------------------------------------
  // 3. 데이터 동기화 (React Effects)
  // useEffect는 특정 조건에서만 코드를 실행하여 불필요한 재실행을 막는 성능 최적화 도구입니다.
  //----------------------------------------------------------------

  /**
   * [Effect 1: 데이터 로딩]
   * 컴포넌트가 처음 마운트되거나, URL의 id가 바뀔 때만 실행됩니다.
   * 서버 API를 호출하여 문서 데이터를 가져오는 역할을 합니다.
   */
  useEffect(() => {
    if (id) { // id가 있는 경우 (기존 문서 수정)
      setIsLoading(true);
      fetch(`/editor/api/documents/${id}`)
        .then(response => {
          if (!response.ok) throw new Error('문서를 불러오는데 실패했습니다.');
          return response.json();
        })
        .then(data => {
          setProjectSettings({
            title: data.title || '',
            coverImage: data.coverImage || '',
            tags: data.tags || [],
          });
          if (data.tiptapJson) {
            let content = data.tiptapJson;
            // 호환성 코드: 과거에 이중으로 문자열화된 데이터가 있다면 객체로 파싱합니다.
            if (typeof content === 'string') {
              try {
                content = JSON.parse(content);
              } catch (e) {
                console.error("Tiptap JSON 파싱 실패:", e);
                content = null; 
              }
            }
            setInitialContent(content); // 파싱된 객체를 상태에 저장
          }
        })
        .catch(error => {
          console.error("문서 로딩 중 오류:", error);
          alert(error.message);
        })
        .finally(() => setIsLoading(false)); // 로딩 완료
    } else { // id가 없는 경우 (새 문서 작성)
      setIsLoading(false);
    }
  }, [id]); // 의존성 배열: 'id'가 바뀔 때만 이 Effect를 재실행합니다.

  /**
   * [Effect 2: 에디터에 내용 채우기]
   * 로딩이 끝나고, 불러온 내용(initialContent)이 준비되면 에디터에 내용을 주입합니다.
   * 로딩과 주입 로직을 분리해야 안정적으로 동작합니다.
   */
  useEffect(() => {
    if (editor && !isLoading && initialContent && editor.isEmpty) {
      editor.commands.setContent(initialContent, false);
    }
  }, [editor, isLoading, initialContent]); // 의존성 배열: 세 값 중 하나라도 바뀌면 재실행

  //----------------------------------------------------------------
  // 4. 이벤트 핸들러 및 헬퍼 함수
  // 사용자의 행동(클릭 등)에 반응하거나, 특정 작업을 수행하는 함수들입니다.
  //----------------------------------------------------------------

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

  const handleSettingsSave = (newSettings) => {
    setProjectSettings(newSettings);
    setIsSettingsModalOpen(false);
  };

  const handlePreviewClick = () => {
    setIsPreviewModalOpen(true);
  };

  const getEditorContent = () => {
    return editor ? editor.getHTML() : '';
  };

  /**
   * [메인 저장 함수] 'Update Project' 버튼 클릭 시 실행됩니다.
   */
  const handleSaveDocument = async () => {
    if (!editor) return;

    if (!projectSettings.title) {
      alert('프로젝트 제목을 입력해주세요.');
      setIsSettingsModalOpen(true);
      return;
    }

    // 서버로 전송할 데이터 객체를 구성합니다.
    const saveRequest = {
      title: projectSettings.title,
      tiptapJson: JSON.stringify(editor.getJSON()), // 에디터 내용을 JSON 문자열로 변환
      htmlBackup: editor.getHTML(),
      coverImage: projectSettings.coverImage,
      tags: projectSettings.tags,
    };
    
    const isUpdating = !!id;
    const url = isUpdating ? `/editor/api/documents/${id}` : '/editor/api/documents';
    const method = isUpdating ? 'PUT' : 'POST';

    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(saveRequest),
      });

      if (response.ok) {
        const documentId = await response.json();
        window.location.href = `/editor/result/${documentId}`;
      } else {
        alert('저장에 실패했습니다.');
      }
    } catch (error) {
      console.error('저장 중 오류:', error);
      alert('저장 중 오류가 발생했습니다.');
    }
  };

  const handleStylesApply = (newStyles) => {
    setEditorStyles(newStyles);
    setIsStylesModalOpen(false);
  };

  //----------------------------------------------------------------
  // 5. JSX 렌더링
  // 이 컴포넌트가 화면에 어떻게 보일지를 정의하는 부분입니다. (HTML과 유사)
  //----------------------------------------------------------------
  return (
    <div className="app-container">
      <div className="main-content">
        {/* 에디터 본문 영역 */}
        <div className="editor-container" style={editorStyles}>
          {editor && <BubbleMenuComponent editor={editor} />}
          <EditorContent editor={editor} />
        </div>

        {/* 사이드바 UI. 필요한 함수와 상태를 props로 전달합니다. */}
        <Sidebar
          editor={editor}
          onEmbedClick={() => setIsEmbedModalOpen(true)}
          onImageAdd={() => setIsImageModalOpen(true)}
          onStylesClick={() => setIsStylesModalOpen(true)}
          onSettingsClick={() => setIsSettingsModalOpen(true)}
          onPhotoGridClick={() => setIsPhotoGridModalOpen(true)}
          onPreviewClick={handlePreviewClick}
          onSaveClick={handleSaveDocument}
        />
      </div>

      {/* 조건부 렌더링: 각 모달의 'isOpen' 상태가 true일 때만 화면에 나타납니다. */}
      {isImageModalOpen && <ImageUploadModal onClose={() => setIsImageModalOpen(false)} onImageAdd={handleImageAdd} />}
      {isEmbedModalOpen && <EmbedModal onClose={() => setIsEmbedModalOpen(false)} onEmbed={handleEmbed} />}
      {isSettingsModalOpen && <SettingsModal onClose={() => setIsSettingsModalOpen(false)} settings={projectSettings} onSave={handleSettingsSave} />}
      {isStylesModalOpen && <StylesModal onClose={() => setIsStylesModalOpen(false)} onStylesApply={handleStylesApply} currentStyles={editorStyles} />}
      {isPhotoGridModalOpen && <PhotoGridModal onClose={() => setIsPhotoGridModalOpen(false)} onGridCreate={handleCreateGrid} />}
      {isPreviewModalOpen && <PreviewModal isOpen={isPreviewModalOpen} onClose={() => setIsPreviewModalOpen(false)} editorContent={getEditorContent()} />}
    </div>
  );
}

export default App;
