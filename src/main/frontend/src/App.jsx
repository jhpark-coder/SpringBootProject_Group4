/**
 * @file App.jsx
 * @description 이 애플리케이션의 메인 컴포넌트입니다. Tiptap 에디터의 초기화, 상태 관리,
 *              데이터 로딩 및 저장, UI 렌더링 등 에디터의 모든 핵심 로직을 담당합니다.
 */

import { useState, useEffect, useRef } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import { useParams, useLocation } from 'react-router-dom';
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
import CodeBlockNode from './CodeBlockNode.jsx';

import BubbleMenuComponent from './BubbleMenuComponent.jsx';
import Sidebar from './Sidebar.jsx';
import ImageUploadModal from './ImageUploadModal.jsx';
import StylesModal from './StylesModal.jsx';
import PhotoGridModal from './PhotoGridModal.jsx';
import PreviewModal from './PreviewModal.jsx';
import EmbedModal from './EmbedModal.jsx';
import SettingsModal from './SettingsModal.jsx';
import VideoUploadModal from './VideoUploadModal';
import AudioUploadModal from './AudioUploadModal';
import './App.css';

const CATEGORIES = {
  '아트워크': ['포토그라피', '일러스트레이션', '스케치', '코믹스'],
  '그래픽디자인': ['타이포그라피', '뮤직패키징', '로고', '그래픽디자인스', '편집'],
  '캐릭터': ['카툰', '애니메', '팬아트', '3D'],
  'Java': ['통신', '알고리즘', 'Thread', 'etc'],
  '프론트엔드': ['HTML', 'CSS', 'Javascript', 'etc'],
  'Python': ['통신', '알고리즘', 'Thread', 'etc'],
};

// highlight.js and lowlight for Code Block
import { createLowlight } from 'lowlight';
import 'highlight.js/styles/github-dark.css';
import js from 'highlight.js/lib/languages/javascript';
import css from 'highlight.js/lib/languages/css';
import html from 'highlight.js/lib/languages/xml';
import python from 'highlight.js/lib/languages/python';
import javaLang from 'highlight.js/lib/languages/java';

// lowlight 인스턴스는 App 컴포넌트 외부에 한 번만 생성합니다.
const lowlight = createLowlight();
lowlight.register('javascript', js);
lowlight.register('css', css);
lowlight.register('html', html);
lowlight.register('python', python);
lowlight.register('java', javaLang);

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
  const location = useLocation(); // 현재 URL 경로 정보
  const hasLoaded = useRef(false); // 로딩 잠금장치
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [isVideoModalOpen, setIsVideoModalOpen] = useState(false);
  const [isAudioModalOpen, setIsAudioModalOpen] = useState(false);
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
    tags: [],
    saleType: '',
    salePrice: '',
    auctionDuration: '',
    startBidPrice: '',
    buyNowPrice: '',
  });
  const [initialContent, setInitialContent] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [editMode, setEditMode] = useState('new'); // 'new', 'edit-auction', 'edit-product', 'edit-editor'

  // 현재 경로에 따라 API 엔드포인트와 편집 모드 결정
  const getApiInfo = () => {
    const path = location.pathname;
    if (path.includes('/editor/auction/')) {
      return {
        endpoint: `/api/auctions/${id}`,
        mode: 'edit-auction',
        title: '경매 편집'
      };
    } else if (path.includes('/editor/product/')) {
      return {
        endpoint: `/api/products/${id}`,
        mode: 'edit-product',
        title: '상품 편집'
      };
    } else if (path.includes('/editor/') && id) {
      return {
        endpoint: `/editor/api/documents/${id}`,
        mode: 'edit-editor',
        title: '문서 편집'
      };
    } else {
      return {
        endpoint: null,
        mode: 'new',
        title: '새 프로젝트'
      };
    }
  };

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
      CodeBlockNode.configure({ lowlight }),
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
    const { endpoint, mode } = getApiInfo();
    setEditMode(mode);

    if (endpoint && id) { // id가 있는 경우 (기존 데이터 편집)
      setIsLoading(true);
      console.log('Loading data from:', endpoint);

      fetch(endpoint)
        .then(response => {
          if (!response.ok) throw new Error('데이터를 불러오는데 실패했습니다.');
          return response.json();
        })
        .then(data => {
          console.log('Loaded data:', data);

          // 공통 필드 처리
          const commonData = {
            title: data.title || data.name || '',
            coverImage: data.coverImage || data.imageUrl || '',
            backgroundColor: data.backgroundColor || '#ffffff',
            fontFamily: data.fontFamily || 'sans-serif',
          };

          // 타입별 특화 처리
          if (mode === 'edit-auction') {
            console.log('경매 편집 데이터 로딩:', data);
            setProjectSettings({
              ...commonData,
              tags: [data.primaryCategory, data.secondaryCategory].filter(Boolean),
              saleType: 'auction',
              salePrice: '',
              auctionDuration: `${data.auctionDuration}일`,
              startBidPrice: data.startBidPrice?.toString() || '',
              buyNowPrice: data.buyNowPrice?.toString() || '',
            });
          } else if (mode === 'edit-product') {
            console.log('상품 편집 데이터 로딩:', data);
            setProjectSettings({
              ...commonData,
              tags: [data.primaryCategory, data.secondaryCategory].filter(Boolean),
              saleType: 'sale',
              salePrice: data.price?.toString() || '',
              auctionDuration: '',
              startBidPrice: '',
              buyNowPrice: '',
            });
          } else {
            // 기존 에디터 로직
            setProjectSettings({
              title: data.title || '',
              coverImage: data.coverImage || '',
              tags: data.tags || [],
              saleType: data.saleType || '',
              salePrice: data.salePrice || '',
              auctionDuration: data.auctionDuration || '',
              startBidPrice: data.startBidPrice || '',
              buyNowPrice: data.buyNowPrice || '',
            });
          }

          setEditorStyles({
            backgroundColor: commonData.backgroundColor,
            fontFamily: commonData.fontFamily,
          });

          // 콘텐츠 처리
          let content = null;
          if (data.tiptapJson) {
            content = data.tiptapJson;
            if (typeof content === 'string') {
              try {
                content = JSON.parse(content);
              } catch (e) {
                console.error("Tiptap JSON 파싱 실패:", e);
                content = null;
              }
            }
          } else if (data.description) {
            // description을 HTML로 처리
            content = {
              type: 'doc',
              content: [
                {
                  type: 'paragraph',
                  content: [
                    {
                      type: 'text',
                      text: 'HTML 내용을 편집하려면 새로 작성해주세요.'
                    }
                  ]
                }
              ]
            };
          }

          if (content) {
            setInitialContent(content);
          }
        })
        .catch(error => {
          console.error("데이터 로딩 중 오류:", error);
          alert(error.message);
        })
        .finally(() => setIsLoading(false));
    } else { // id가 없는 경우 (새 문서 작성)
      setIsLoading(false);
    }
  }, [id, location.pathname]); // 의존성 배열: 'id'나 경로가 바뀔 때만 이 Effect를 재실행합니다.

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

  const extractSrcFromIframe = (iframeCode) => {
    const srcMatch = iframeCode.match(/src=["']([^"']+)["']/);
    return srcMatch ? srcMatch[1] : null;
  };

  const handleEmbed = (urlOrIframe) => {
    if (!urlOrIframe || !editor) return;

    let embedUrl = null;

    if (urlOrIframe.includes('<iframe')) {
      // iframe 코드에서 src URL 추출
      embedUrl = extractSrcFromIframe(urlOrIframe);
      if (!embedUrl) {
        alert('iframe 코드에서 src URL을 찾을 수 없습니다.');
        return;
      }
    } else {
      // 일반 URL 처리
      const videoId = getYoutubeVideoId(urlOrIframe);
      if (videoId) {
        embedUrl = `https://www.youtube.com/embed/${videoId}`;
      } else {
        // 다른 임베드 URL은 그대로 사용
        embedUrl = urlOrIframe;
      }
    }

    if (embedUrl) {
      console.log('임베드 URL:', embedUrl);
      editor.chain().focus().setIframe({ src: embedUrl }).run();
      setIsEmbedModalOpen(false);
    } else {
      alert('유효한 유튜브 URL 또는 아이프레임 코드를 입력해주세요.');
    }
  };

  const handleCreateGrid = (gridData) => {
    editor.chain().focus().setPhotoGrid(gridData).run();
    setIsPhotoGridModalOpen(false);
  };

  const handleImageAdd = ({ src, alt }) => {
    editor.chain().focus().setImage({ src, alt }).run();
    setIsImageModalOpen(false);
  };

  const handleVideoAdd = (videoData) => {
    editor.chain().focus().setVideo(videoData).run();
    setIsVideoModalOpen(false);
  };

  const handleAudioAdd = (audioData) => {
    editor.chain().focus().setAudio(audioData).run();
    setIsAudioModalOpen(false);
  };

  const handleSettingsSave = (newSettings) => {
    setProjectSettings(newSettings);
    setIsSettingsModalOpen(false);
  };

  const handlePreviewClick = () => {
    setIsPreviewModalOpen(true);
  };

  const getEditorContent = () => {
    if (!editor) {
      return '';
    }
    return editor.getHTML();
  };

  const handleSaveDocument = async () => {
    if (!editor) return;

    const { saleType } = projectSettings;

    if (!saleType) {
      alert("프로젝트 설정을 열어 판매 또는 경매 정보를 입력해주세요.");
      setIsSettingsModalOpen(true);
      return;
    }

    const tiptapJson = editor.getJSON();
    const htmlBackup = editor.getHTML();

    let url;
    let method;
    let requestData;
    let redirectUrl;

    const commonData = {
      name: projectSettings.title,
      imageUrl: projectSettings.coverImage,
      primaryCategory: projectSettings.tags.find(tag => Object.keys(CATEGORIES).includes(tag)),
      secondaryCategory: projectSettings.tags.find(tag => !(Object.keys(CATEGORIES).includes(tag))),
      tiptapJson: JSON.stringify(tiptapJson),
      htmlBackup: htmlBackup,
      backgroundColor: editorStyles.backgroundColor,
      fontFamily: editorStyles.fontFamily,
    };

    // 편집 모드에 따른 URL과 HTTP 메서드 결정
    const isEditMode = editMode.startsWith('edit-');

    if (saleType === 'sale') {
      if (isEditMode && id) {
        url = `/api/products/${id}`;
        method = 'PUT';
      } else {
        url = '/api/products';
        method = 'POST';
      }
      requestData = {
        ...commonData,
        price: parseInt(projectSettings.salePrice),
      };
      redirectUrl = (responseId) => `http://localhost:8080/result/product/${responseId || id}`;
    } else if (saleType === 'auction') {
      if (isEditMode && id) {
        url = `/api/auctions/${id}`;
        method = 'PUT';
      } else {
        url = '/api/auctions';
        method = 'POST';
      }
      requestData = {
        ...commonData,
        auctionDuration: parseInt(projectSettings.auctionDuration.replace('일', '')),
        startBidPrice: parseInt(projectSettings.startBidPrice),
        buyNowPrice: parseInt(projectSettings.buyNowPrice),
      };
      redirectUrl = (responseId) => `http://localhost:8080/result/auction/${responseId || id}`;
    } else {
      // 기존 저장 로직 (Editor) - 지금은 사용하지 않음
      return;
    }

    console.log('Edit mode:', editMode);
    console.log('Is edit mode:', isEditMode);
    console.log('Current ID:', id);
    console.log('HTTP method:', method);
    console.log('Target URL:', url);
    console.log('Saving document with data:', requestData);

    try {
      const response = await fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData),
      });

      console.log('Response status:', response.status);
      console.log('Response headers:', response.headers);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Server error response:', errorText);
        throw new Error(`저장에 실패했습니다 (${response.status}): ${errorText}`);
      }

      const savedId = await response.json();
      console.log('Document saved successfully with ID:', savedId);

      // 편집 모드일 때는 기존 ID 사용, 새 작성일 때는 응답받은 ID 사용
      const finalId = isEditMode ? id : savedId;
      window.location.href = redirectUrl(finalId);

    } catch (error) {
      console.error('Error saving document:', error);

      // 네트워크 오류와 서버 오류를 구분
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        alert('네트워크 오류가 발생했습니다. 서버가 실행 중인지 확인해주세요.');
      } else {
        alert(`저장 중 오류가 발생했습니다: ${error.message}`);
      }
    }
  };

  const handleStyleChange = (newStyles) => {
    setEditorStyles(newStyles);
  };

  const handleStylesModalClose = () => {
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
          onVideoAdd={() => setIsVideoModalOpen(true)}
          onAudioAdd={() => setIsAudioModalOpen(true)}
          onStylesClick={() => setIsStylesModalOpen(true)}
          onSettingsClick={() => setIsSettingsModalOpen(true)}
          onPhotoGridClick={() => setIsPhotoGridModalOpen(true)}
          onPreviewClick={handlePreviewClick}
          onSaveClick={handleSaveDocument}
        />
      </div>

      {/* 조건부 렌더링: 각 모달의 'isOpen' 상태가 true일 때만 화면에 나타납니다. */}
      <div className="modals">
        {isImageModalOpen && (
          <ImageUploadModal onClose={() => setIsImageModalOpen(false)} onImageAdd={handleImageAdd} />
        )}
        {isVideoModalOpen && (
          <VideoUploadModal onClose={() => setIsVideoModalOpen(false)} onVideoAdd={handleVideoAdd} />
        )}
        {isAudioModalOpen && (
          <AudioUploadModal onClose={() => setIsAudioModalOpen(false)} onAudioAdd={handleAudioAdd} />
        )}
        {isEmbedModalOpen && (
          <EmbedModal onClose={() => setIsEmbedModalOpen(false)} onEmbed={handleEmbed} />
        )}
        {isSettingsModalOpen && <SettingsModal onClose={() => setIsSettingsModalOpen(false)} settings={projectSettings} onSave={handleSettingsSave} />}
        {isStylesModalOpen && <StylesModal isOpen={isStylesModalOpen} onClose={handleStylesModalClose} onStyleChange={handleStyleChange} currentStyles={editorStyles} />}
        {isPhotoGridModalOpen && (
          <PhotoGridModal
            onClose={() => setIsPhotoGridModalOpen(false)}
            onGridCreate={handleCreateGrid}
          />
        )}
        {isPreviewModalOpen && <PreviewModal isOpen={isPreviewModalOpen} onClose={() => setIsPreviewModalOpen(false)} editorContent={getEditorContent()} styles={editorStyles} />}
      </div>
    </div>
  );
}

export default App;
