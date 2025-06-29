/**
 * @file App.jsx
 * @description 이 애플리케이션의 메인 컴포넌트입니다. Tiptap 에디터의 초기화, 상태 관리,
 *              데이터 로딩 및 저장, UI 렌더링 등 에디터의 모든 핵심 로직을 담당합니다.
 */

//================================================================================
// 0. 모듈 임포트 (Importing Modules)
// 리액트와 다른 라이브러리에서 필요한 기능들을 가져옵니다.
// 'import'는 다른 파일에 있는 코드(컴포넌트, 함수, 변수 등)를 현재 파일에서 사용할 수 있게 해주는 문법입니다.
// 이를 통해 코드를 재사용하고 모듈별로 정리할 수 있습니다.
//================================================================================
import React, { useState, useEffect, useRef, lazy, Suspense } from 'react'; // React의 핵심 기능(Hooks)을 가져옵니다.
import { useEditor, EditorContent } from '@tiptap/react'; // Tiptap 에디터의 React 버전을 가져옵니다.
import { useParams, useLocation } from 'react-router-dom'; // URL 경로를 다루기 위한 기능을 가져옵니다.
import StarterKit from '@tiptap/starter-kit'; // Tiptap의 기본 확장 기능 모음입니다. (볼드, 이탤릭 등)
import CustomImage from './CustomImage.jsx'; // 우리가 직접 만든 커스텀 이미지 노드를 가져옵니다.
import Underline from '@tiptap/extension-underline'; // 밑줄 기능을 Tiptap에 추가하기 위해 가져옵니다.
import Link from '@tiptap/extension-link'; // 링크 기능을 Tiptap에 추가하기 위해 가져옵니다.
import TextAlign from '@tiptap/extension-text-align'; // 텍스트 정렬 기능을 Tiptap에 추가하기 위해 가져옵니다.
import TextStyle from '@tiptap/extension-text-style'; // 텍스트 스타일(예: 색상)을 다루기 위한 확장입니다.
import { mergeAttributes, Extension } from '@tiptap/core'; // Tiptap 확장 기능을 직접 만들 때 필요한 도구들입니다.
import Iframe from './Iframe.jsx'; // 유튜브 영상 등을 삽입하기 위한 Iframe 노드입니다.
import VideoNode from './VideoNode.jsx'; // 비디오 파일을 위한 커스텀 노드입니다.
import AudioNode from './AudioNode.jsx'; // 오디오 파일을 위한 커스텀 노드입니다.
import PhotoGridNode from './PhotoGridNode.jsx'; // 여러 이미지를 격자 형태로 보여주는 포토그리드 노드입니다.
import PaywallNode from './PaywallNode.jsx'; // 유료 콘텐츠 영역을 표시하기 위한 페이월 노드입니다.
import CodeBlockNode from './CodeBlockNode.jsx'; // 코드 블록을 위한 커스텀 노드입니다.
import SpacerNode from './SpacerNode.jsx'; // 공백 영역을 표시하기 위한 스페이서 노드입니다.

// 동적 임포트로 큰 컴포넌트들을 지연 로딩
const SettingsModal = lazy(() => import('./SettingsModal.jsx'));
const Sidebar = lazy(() => import('./Sidebar.jsx'));
const BubbleMenuComponent = lazy(() => import('./BubbleMenuComponent.jsx'));
const ImageUploadModal = lazy(() => import('./ImageUploadModal.jsx'));
const VideoUploadModal = lazy(() => import('./VideoUploadModal.jsx'));
const AudioUploadModal = lazy(() => import('./AudioUploadModal.jsx'));
const PhotoGridModal = lazy(() => import('./PhotoGridModal.jsx'));
const PreviewModal = lazy(() => import('./PreviewModal.jsx'));
const EmbedModal = lazy(() => import('./EmbedModal.jsx'));
const StylesModal = lazy(() => import('./StylesModal.jsx'));
const SpacerModal = lazy(() => import('./SpacerModal.jsx'));

// 카테고리 데이터입니다. 자바스크립트의 객체(Object) 형태로, 키(key)와 값(value)으로 이루어져 있습니다.
const CATEGORIES = {
  '아트워크': ['포토그라피', '일러스트레이션', '스케치', '코믹스'],
  '그래픽디자인': ['타이포그라피', '뮤직패키징', '로고', '그래픽디자인스', '편집'],
  '캐릭터': ['카툰', '애니메', '팬아트', '3D'],
  'Java': ['통신', '알고리즘', 'Thread', 'etc'],
  '프론트엔드': ['HTML', 'CSS', 'Javascript', 'etc'],
  'Python': ['통신', '알고리즘', 'Thread', 'etc'],
};

//--------------------------------------------------------------------------------
// 코드 블록에 구문 강조(Syntax Highlighting)를 적용하기 위한 설정입니다.
// `highlight.js`와 `lowlight` 라이브러리를 사용합니다.
//--------------------------------------------------------------------------------
import { createLowlight } from 'lowlight'; // lowlight 라이브러리에서 핵심 함수를 가져옵니다.
import 'highlight.js/styles/github-dark.css'; // 코드 블록의 디자인 테마(github-dark)를 가져옵니다.
import js from 'highlight.js/lib/languages/javascript'; // 각 프로그래밍 언어별 구문 강조 규칙을 가져옵니다.
import css from 'highlight.js/lib/languages/css';
import html from 'highlight.js/lib/languages/xml'; // HTML은 XML 규칙을 사용합니다.
import python from 'highlight.js/lib/languages/python';
import javaLang from 'highlight.js/lib/languages/java';

// lowlight 인스턴스는 App 컴포넌트 외부에 한 번만 생성합니다.
// 이렇게 하면 컴포넌트가 리렌더링될 때마다 비싼 생성 작업을 반복하지 않아 성능에 유리합니다.
const lowlight = createLowlight();
// lowlight에 각 언어를 등록해야 해당 언어의 코드 하이라이팅을 사용할 수 있습니다.
lowlight.register('javascript', js);
lowlight.register('css', css);
lowlight.register('html', html);
lowlight.register('python', python);
lowlight.register('java', javaLang);

// React의 contentEditable 경고 억제
const originalConsoleError = console.error;
console.error = (...args) => {
  if (args[0] && typeof args[0] === 'string' && args[0].includes('contentEditable')) {
    return; // contentEditable 경고 억제
  }
  originalConsoleError.apply(console, args);
};

//================================================================================
// Tiptap 확장 기능 직접 만들기 (커스텀 FontSize)
// Tiptap의 큰 장점은 기본 제공 기능 외에 우리가 원하는 기능을 직접 만들어 추가할 수 있다는 점입니다.
// 여기서는 글자 크기를 조절하는 'FontSize'라는 새로운 확장(Extension)을 만들고 있습니다.
// Tiptap은 이런 식으로 필요한 기능을 직접 만들어 붙일 수 있는 유연한 구조를 가집니다.
//================================================================================
const FontSize = Extension.create({ // Extension.create를 사용하여 새로운 확장 기능을 정의합니다.
  name: 'fontSize', // 이 확장의 고유한 이름입니다.

  // addGlobalAttributes: 에디터의 모든 'textStyle' 타입에 'fontSize' 속성을 추가하는 설정입니다.
  // 이를 통해 Tiptap이 'fontSize'를 인식하고 처리할 수 있게 됩니다.
  addGlobalAttributes() {
    return [
      {
        types: ['textStyle'],
        attributes: {
          fontSize: {
            default: null, // 기본값은 null로, 아무것도 적용되지 않은 상태입니다.
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

  // addCommands: 이 확장에서 사용할 명령어들을 정의합니다.
  // 이 명령어들은 에디터의 버튼 등을 클릭했을 때 실행됩니다.
  addCommands() {
    return {
      // setFontSize: 글자 크기를 설정하는 명령어입니다.
      // `fontSize => ({ chain }) => { ... }`는 명령어를 실행하는 함수를 정의하는 Tiptap의 방식입니다.
      setFontSize: fontSize => ({ chain }) => {
        return chain()
          .setMark('textStyle', { fontSize }) // 선택된 텍스트에 'textStyle' 마크를 적용하고, fontSize 속성을 설정합니다.
          .run() // 정의된 명령어 체인을 실행합니다.
      },
      // unsetFontSize: 설정된 글자 크기를 제거하는 명령어입니다.
      unsetFontSize: () => ({ chain }) => {
        return chain()
          .setMark('textStyle', { fontSize: null }) // fontSize 속성을 null로 만들어 제거합니다.
          .removeEmptyTextStyle() // 비어있는 textStyle 태그를 정리합니다.
          .run()
      },
    }
  },
})

// Google Fonts 목록 (StylesModal과 동일)
const GOOGLE_FONTS = [
  // Korean
  { name: 'Noto Sans KR', family: "'Noto Sans KR', sans-serif" },
  { name: 'Nanum Gothic', family: "'Nanum Gothic', sans-serif" },
  { name: 'Black Han Sans', family: "'Black Han Sans', sans-serif" },
  // English
  { name: 'Roboto', family: "'Roboto', sans-serif" },
  { name: 'Montserrat', family: "'Montserrat', sans-serif" },
  { name: 'Playfair Display', family: "'Playfair Display', serif" },
  // Monospace for code
  { name: 'Source Code Pro', family: "'Source Code Pro', monospace" }
];

// Google Fonts 로딩 함수
const loadGoogleFont = (fontName) => {
  const fontId = `google-font-${fontName.replace(/\s/g, '-')}`;
  if (document.getElementById(fontId)) {
    return; // 이미 로드된 폰트는 다시 로드하지 않음
  }
  const link = document.createElement('link');
  link.id = fontId;
  link.href = `https://fonts.googleapis.com/css2?family=${fontName.replace(/\s/g, '+')}:wght@400;700&display=swap`;
  link.rel = 'stylesheet';
  document.head.appendChild(link);
};

//================================================================================
// 메인 애플리케이션 컴포넌트
// React에서 컴포넌트는 UI를 독립적인 부분으로 나누어 재사용할 수 있게 해주는 코드 조각입니다.
// 이 'App' 함수가 바로 이 웹 페이지의 전체 구조를 정의하는 메인 컴포넌트입니다.
// 함수 이름이 대문자로 시작하는 것이 React 컴포넌트의 규칙입니다.
//================================================================================
function App() {
  const [dbContent, setDbContent] = useState('');
  const [isStylesModalOpen, setIsStylesModalOpen] = useState(false);
  // ... (다른 useState 선언들)

  // 컴포넌트가 마운트될 때 모든 Google Fonts를 미리 로드합니다.
  React.useEffect(() => {
    GOOGLE_FONTS.forEach(font => loadGoogleFont(font.name));
  }, []);

  //----------------------------------------------------------------
  // 1. 상태 관리 (State Management)
  // '상태(State)'는 컴포넌트가 기억해야 할 동적인 데이터입니다. 예를 들어, 모달 창이 열려 있는지 여부,
  // 사용자가 입력한 텍스트, 서버에서 받아온 데이터 등이 상태가 될 수 있습니다.
  // React의 `useState`라는 특별한 함수(Hook)를 사용해 상태를 만듭니다.
  // `useState`는 [현재 상태 값, 상태를 변경하는 함수] 형태의 배열을 반환합니다.
  // 중요한 점은, 상태를 변경하는 함수(예: setIsImageModalOpen)가 호출되어 상태값이 바뀌면,
  // React는 이 컴포넌트를 화면에 자동으로 다시 그려서(리렌더링) 변경사항을 반영합니다.
  //----------------------------------------------------------------

  // `useParams`와 `useLocation`은 react-router-dom 라이브러리가 제공하는 훅(Hook)입니다.
  // 훅(Hook)은 함수 컴포넌트에서 React의 기능(상태, 생명주기 등)을 "연결"할 수 있게 해주는 함수입니다.
  const { id } = useParams(); // URL 파라미터에서 문서 ID를 가져옵니다. (예: /editor/123 -> id는 '123')
  const location = useLocation(); // 현재 URL의 전체 경로 정보(예: '/editor/123')를 가져옵니다.

  // `useRef`는 렌더링과 상관없이 값을 유지해야 할 때 사용합니다.
  // `.current` 프로퍼티에 값을 저장하며, 이 값이 바뀐다고 해서 화면이 다시 렌더링되지는 않습니다.
  // 여기서는 데이터가 한번 로딩되었는지 여부를 체크하기 위한 '잠금장치' 역할로 사용됩니다.
  const hasLoaded = useRef(false);

  // 각 모달(팝업창)이 열려있는지(true) 닫혀있는지(false)를 기억하기 위한 상태들입니다.
  // 예를 들어 `isImageModalOpen`이 `true`가 되면 이미지 업로드 모달이 화면에 나타납니다.
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [isVideoModalOpen, setIsVideoModalOpen] = useState(false);
  const [isAudioModalOpen, setIsAudioModalOpen] = useState(false);
  const [isEmbedModalOpen, setIsEmbedModalOpen] = useState(false);
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false);
  const [isPhotoGridModalOpen, setIsPhotoGridModalOpen] = useState(false);
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);
  const [isSpacerModalOpen, setIsSpacerModalOpen] = useState(false);

  // 스페이서 모달 관련 상태 통합
  const [spacerModalConfig, setSpacerModalConfig] = useState({
    currentHeight: '2rem',
    onSave: () => { },
  });

  // 에디터 자체의 스타일(배경색, 글꼴)을 기억하기 위한 상태입니다.
  const [editorStyles, setEditorStyles] = useState({
    backgroundColor: '#ffffff',
    fontFamily: 'sans-serif',
  });

  // 프로젝트 전체 설정(제목, 커버이미지, 판매 정보 등)을 기억하기 위한 상태입니다.
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

  // 서버에서 불러온 에디터의 초기 콘텐츠를 임시로 저장하는 상태입니다.
  const [initialContent, setInitialContent] = useState(null);
  // 데이터를 로딩하는 중인지 여부를 기억하는 상태입니다. 로딩 중일 때 로딩 스피너 등을 보여줄 수 있습니다.
  const [isLoading, setIsLoading] = useState(true);
  // 현재 페이지가 '새 글 작성'인지, '기존 글 편집'인지를 구분하기 위한 상태입니다.
  const [editMode, setEditMode] = useState('new'); // 'new', 'edit-auction', 'edit-product', 'edit-editor'

  // 현재 경로에 따라 API 엔드포인트와 편집 모드 결정
  // 이 함수는 현재 브라우저의 URL 주소를 보고, 어떤 데이터를 어디서 가져와야 할지 결정합니다.
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
  // `useEditor` 훅(hook)을 사용하여 Tiptap 에디터 인스턴스를 생성하고 설정합니다.
  // 이 설정은 컴포넌트가 처음 생성될 때 한 번만 실행됩니다.
  //----------------------------------------------------------------
  const editor = useEditor({
    // 에디터에서 사용할 확장 기능들을 배열로 등록합니다. 여기에 등록해야 해당 기능을 쓸 수 있습니다.
    extensions: [
      StarterKit,      // 기본 기능(단락, 볼드, 헤딩 등) 모음
      CustomImage,     // 커스텀 이미지 기능
      Underline,       // 밑줄 기능
      Link.configure({ openOnClick: false }), // 링크 기능 (클릭 시 바로 이동하지 않도록 설정)
      TextAlign.configure({ // 텍스트 정렬 기능
        types: ['heading', 'paragraph', 'image', 'videoPlayer', 'iframe', 'audio'], // 정렬을 적용할 요소 타입들
        addCssClasses: true, // 정렬 시 CSS 클래스를 추가
      }),
      TextStyle.configure({ // 텍스트 스타일 확장
        HTMLAttributes: {
          class: 'custom-text-style',
        },
      }),
      FontSize,        // 위에서 직접 만든 글자 크기 기능
      Iframe,          // Iframe(유튜브 등) 삽입 기능
      VideoNode,       // 비디오 파일 삽입 기능
      AudioNode,       // 오디오 파일 삽입 기능
      PhotoGridNode,   // 포토 그리드 삽입 기능
      PaywallNode,     // 유료 콘텐츠 영역 기능
      CodeBlockNode.configure({ lowlight }), // 코드 블록 기능 (구문 강조 포함)
      SpacerNode,      // 공백 삽입 기능
    ],
    // 에디터의 초기 내용은 항상 비워둡니다. 서버에서 비동기적으로 데이터를 불러온 후 채워넣을 것입니다.
    content: '',
    // 에디터의 HTML 최상위 요소에 적용될 속성입니다. (CSS 클래스 등)
    editorProps: {
      attributes: {
        class: 'prose-mirror-editor',
      },
    },
    // 에디터의 내용이 변경될 때마다 실행되는 함수입니다.
    // 여기서는 자동 저장 기능을 구현하거나, 내용이 변경되었음을 감지하는 데 사용할 수 있습니다.
    onCreate: ({ editor }) => {
      // 스페이서 '수정' 로직: 에디터 스토리지에 함수 저장
      editor.storage.spacer = {
        openModal: (currentHeight, onUpdateCallback) => {
          setSpacerModalConfig({
            currentHeight,
            onSave: (newHeight) => {
              onUpdateCallback(newHeight);
              setIsSpacerModalOpen(false);
            },
          });
          setIsSpacerModalOpen(true);
        },
      };
    },
  });

  //----------------------------------------------------------------
  // 3. 데이터 동기화 (React Effects)
  // `useEffect`는 React의 또 다른 중요한 훅(Hook)입니다.
  // 주로 컴포넌트가 화면에 그려진 후(렌더링 후) 실행하고 싶은 작업들(Side Effects)을 처리할 때 사용합니다.
  // 예를 들어, 서버에 데이터를 요청(API 호출)하거나, 수동으로 DOM을 조작하는 등의 작업을 여기서 수행합니다.
  // `useEffect`는 두 번째 인자인 '의존성 배열'에 따라 실행 시점이 결정됩니다.
  //----------------------------------------------------------------

  /**
   * [Effect 1: 데이터 로딩]
   * 이 `useEffect`는 컴포넌트가 처음 화면에 나타날 때, 또는 URL의 id나 경로가 바뀔 때 실행됩니다.
   * `[id, location.pathname]` 의존성 배열 때문에 id나 경로가 바뀔 때마다 서버에서 새 데이터를 불러옵니다.
   * 서버 API를 호출하여 문서 데이터를 가져오는 역할을 합니다.
   */
  useEffect(() => {
    // 현재 URL을 분석하여 어떤 API에서 데이터를 가져올지 결정합니다.
    const { endpoint, mode } = getApiInfo();
    // 편집 모드를 상태에 저장합니다.
    setEditMode(mode);

    // `endpoint`와 `id`가 있어야만 (즉, 기존 글을 수정하는 경우에만) 아래 코드를 실행합니다.
    if (endpoint && id) { // id가 있는 경우 (기존 데이터 편집)
      setIsLoading(true); // 로딩 시작을 알리기 위해 상태를 true로 변경합니다.
      console.log('Loading data from:', endpoint);

      // `fetch`는 자바스크립트 내장 함수로, 서버에 네트워크 요청을 보낼 때 사용합니다.
      fetch(endpoint)
        // .then()은 비동기 작업이 성공했을 때 실행될 코드를 정의합니다.
        .then(response => {
          if (!response.ok) throw new Error('데이터를 불러오는데 실패했습니다.'); // 응답이 실패하면 에러를 발생시킵니다.
          return response.json(); // 성공 시, 응답 데이터를 JSON 형태로 변환하여 다음 then으로 넘깁니다.
        })
        .then(data => {
          console.log('Loaded data:', data);

          // 서버에서 받은 데이터를 화면에 표시하기 좋게 공통 형식으로 정리합니다.
          const commonData = {
            title: data.title || data.name || '',
            coverImage: data.coverImage || data.imageUrl || '',
            backgroundColor: data.backgroundColor || '#ffffff',
            fontFamily: data.fontFamily || 'sans-serif',
            workDescription: data.workDescription || '',
          };

          // 편집 모드(경매, 상품)에 따라 받아온 데이터를 다르게 처리하여 `projectSettings` 상태에 저장합니다.
          if (mode === 'edit-auction') {
            console.log('경매 편집 데이터 로딩:', data);
            setProjectSettings({
              ...commonData, // 위에서 정의한 공통 데이터를 복사해 넣습니다.
              tags: [data.primaryCategory, data.secondaryCategory].filter(Boolean),
              saleType: 'auction',
              salePrice: '', // 경매는 판매 가격이 없으므로 비워둡니다.
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
              auctionDuration: '', // 상품은 경매 기간이 없으므로 비워둡니다.
              startBidPrice: '',
              buyNowPrice: '',
            });
          } else {
            // 기존 에디터 로직 (일반 문서 편집)
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

          // 에디터의 배경색, 글꼴 등 스타일 정보를 상태에 저장합니다.
          setEditorStyles({
            backgroundColor: commonData.backgroundColor,
            fontFamily: commonData.fontFamily,
          });

          // Google Fonts 로딩 (편집 모드에서 폰트 불러오기)
          if (commonData.fontFamily) {
            const selectedFont = GOOGLE_FONTS.find(f => f.family === commonData.fontFamily);
            if (selectedFont) {
              console.log('Loading Google Font for edit mode:', selectedFont.name);
              loadGoogleFont(selectedFont.name);
            }
          }

          // 에디터에 들어갈 콘텐츠(본문)를 처리합니다.
          let content = null;
          // Tiptap의 JSON 형식이 있으면 그것을 사용합니다.
          if (data.tiptapJson) {
            content = data.tiptapJson;
            // 만약 JSON이 문자열 형태로 왔다면, 자바스크립트 객체로 변환(파싱)합니다.
            if (typeof content === 'string') {
              try {
                content = JSON.parse(content);
              } catch (e) {
                console.error("Tiptap JSON 파싱 실패:", e);
                content = null;
              }
            }
            // Tiptap JSON은 없고 HTML(description)만 있는 경우, 경고 메시지를 담은 기본 콘텐츠를 생성합니다.
          } else if (data.description) {
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

          // 최종적으로 처리된 콘텐츠가 있으면, `initialContent` 상태에 저장합니다.
          if (content) {
            setInitialContent(content);
          }
        })
        // .catch()는 fetch나 .then() 과정에서 에러가 발생했을 때 실행될 코드를 정의합니다.
        .catch(error => {
          console.error("데이터 로딩 중 오류:", error);
          alert(error.message);
        })
        // .finally()는 성공/실패 여부와 상관없이 항상 마지막에 실행될 코드를 정의합니다.
        .finally(() => setIsLoading(false)); // 로딩이 끝났음을 알리기 위해 상태를 false로 변경합니다.
    } else { // id가 없는 경우 (새 문서 작성)
      setIsLoading(false); // 로딩할 데이터가 없으므로 바로 로딩 상태를 해제합니다.
    }
  }, [id, location.pathname]); // 의존성 배열: 'id'나 'location.pathname'이 바뀔 때만 이 Effect를 재실행합니다.

  /**
   * [Effect 2: 에디터에 내용 채우기]
   * 이 `useEffect`는 데이터 로딩이 끝나고, 불러온 내용(`initialContent`)이 준비되면 에디터에 내용을 주입합니다.
   * 로딩과 주입 로직을 분리해야, 에디터가 완전히 준비되지 않은 상태에서 내용을 넣으려는 시도를 막아 안정적으로 동작합니다.
   */
  useEffect(() => {
    // editor가 존재하고, 로딩이 끝났고, initialContent가 있으며, 에디터가 비어있을 때만 실행
    if (editor && !isLoading && initialContent && editor.isEmpty) {
      // editor.commands.setContent()는 에디터의 내용을 설정하는 Tiptap 명령어입니다.
      editor.commands.setContent(initialContent, false);
    }
  }, [editor, isLoading, initialContent]); // 의존성 배열: 세 값 중 하나라도 바뀌면 이 Effect를 재실행합니다.

  //----------------------------------------------------------------
  // 4. 이벤트 핸들러 및 헬퍼 함수
  // 사용자의 행동(클릭 등)에 반응하거나, 특정 작업을 수행하는 함수들입니다.
  // 이 함수들은 보통 UI 요소(버튼 등)의 `onClick` 같은 속성에 연결됩니다.
  // 'handle'이라는 이름은 관례적으로 '이벤트를 처리한다'는 의미로 사용됩니다.
  //----------------------------------------------------------------

  /**
   * 유튜브 URL에서 비디오 ID를 추출하는 헬퍼 함수입니다.
   * @param {string} url - 유튜브 URL
   * @returns {string|null} - 비디오 ID 또는 null
   */
  const getYoutubeVideoId = (url) => {
    if (!url) return null;
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
    const match = url.match(regExp);
    if (match && match[2].length === 11) {
      return match[2];
    }
    return null;
  };

  /**
   * Iframe HTML 코드에서 'src' 속성값(URL)을 추출하는 헬퍼 함수입니다.
   * @param {string} iframeCode - Iframe HTML 태그 문자열
   * @returns {string|null} - 추출된 src URL 또는 null
   */
  const extractSrcFromIframe = (iframeCode) => {
    const srcMatch = iframeCode.match(/src=["']([^"']+)["']/);
    return srcMatch ? srcMatch[1] : null;
  };

  /**
   * 임베드 모달에서 '확인'을 눌렀을 때 실행되는 이벤트 핸들러입니다.
   * 입력된 URL 또는 Iframe 코드를 분석하여 에디터에 임베드 콘텐츠를 삽입합니다.
   * @param {string} urlOrIframe - 사용자가 입력한 URL 또는 Iframe 코드
   */
  const handleEmbed = (urlOrIframe) => {
    if (!urlOrIframe || !editor) return;

    let embedUrl = null;

    // 입력값이 '<iframe'을 포함하면 Iframe 코드로 간주합니다.
    if (urlOrIframe.includes('<iframe')) {
      // iframe 코드에서 src URL 추출
      embedUrl = extractSrcFromIframe(urlOrIframe);
      if (!embedUrl) {
        alert('iframe 코드에서 src URL을 찾을 수 없습니다.');
        return;
      }
    } else {
      // 일반 URL 처리: 유튜브 URL인지 확인합니다.
      const videoId = getYoutubeVideoId(urlOrIframe);
      if (videoId) {
        // 유튜브 URL이면 표준 임베드 URL 형식으로 변환합니다.
        embedUrl = `https://www.youtube.com/embed/${videoId}`;
      } else {
        // 유튜브가 아닌 다른 URL은 그대로 사용합니다.
        embedUrl = urlOrIframe;
      }
    }

    if (embedUrl) {
      console.log('임베드 URL:', embedUrl);
      // Tiptap 명령어를 사용하여 에디터에 Iframe을 삽입합니다.
      editor.chain().focus().setIframe({ src: embedUrl }).run();
      // 임베드 모달을 닫습니다.
      setIsEmbedModalOpen(false);
    } else {
      alert('유효한 유튜브 URL 또는 아이프레임 코드를 입력해주세요.');
    }
  };

  /**
   * 포토 그리드 모달에서 '생성'을 눌렀을 때 실행되는 이벤트 핸들러입니다.
   * @param {object} gridData - 생성할 그리드의 데이터 (레이아웃, 이미지 목록 등)
   */
  const handleCreateGrid = (gridData) => {
    editor.chain().focus().setPhotoGrid(gridData).run();
    setIsPhotoGridModalOpen(false); // 모달을 닫습니다.
  };

  /**
   * 이미지 업로드 모달에서 이미지를 추가했을 때 실행되는 이벤트 핸들러입니다.
   * @param {object} imageData - 추가할 이미지의 데이터 ({ src, alt })
   */
  const handleImageAdd = ({ src, alt }) => {
    editor.chain().focus().setImage({ src, alt }).run();
    setIsImageModalOpen(false); // 모달을 닫습니다.
  };

  /**
   * 비디오 업로드 모달에서 비디오를 추가했을 때 실행되는 이벤트 핸들러입니다.
   * @param {object} videoData - 추가할 비디오의 데이터
   */
  const handleVideoAdd = (videoData) => {
    console.log('handleVideoAdd called with:', videoData);
    console.log('videoData type:', typeof videoData);
    console.log('videoData.src:', videoData.src);
    console.log('videoData.src type:', typeof videoData.src);

    if (!videoData || !videoData.src) {
      console.error('Invalid video data:', videoData);
      alert('비디오 데이터가 유효하지 않습니다.');
      return;
    }

    editor.chain().focus().setVideo(videoData).run();
    setIsVideoModalOpen(false); // 모달을 닫습니다.
  };

  /**
   * 오디오 업로드 모달에서 오디오를 추가했을 때 실행되는 이벤트 핸들러입니다.
   * @param {object} audioData - 추가할 오디오의 데이터
   */
  const handleAudioAdd = (audioData) => {
    editor.chain().focus().setAudio(audioData).run();
    setIsAudioModalOpen(false); // 모달을 닫습니다.
  };

  /**
   * 프로젝트 설정 모달에서 '저장'을 눌렀을 때 실행되는 이벤트 핸들러입니다.
   * @param {object} newSettings - 새로 저장될 설정 값들
   */
  const handleSettingsSave = (newSettings) => {
    setProjectSettings(newSettings);
    setIsSettingsModalOpen(false); // 모달을 닫습니다.
    console.log("Updated project settings:", newSettings);
  };

  /**
   * '미리보기' 버튼을 클릭했을 때 실행되는 이벤트 핸들러입니다.
   */
  const handlePreviewClick = () => {
    setIsPreviewModalOpen(true); // 미리보기 모달을 엽니다.
  };

  /**
   * 에디터의 현재 내용을 HTML 문자열과 JSON 객체로 가져오는 헬퍼 함수입니다.
   * @returns {{htmlContent: string, jsonContent: object}} - 에디터 내용의 HTML과 JSON
   */
  const getEditorContent = () => {
    if (!editor) {
      return { htmlContent: '', jsonContent: null };
    }
    return {
      htmlContent: editor.getHTML(),
      jsonContent: editor.getJSON(),
    };
  };

  /**
   * 문서 저장/제출 함수
   * 에디터 상단의 '저장' 또는 '제출' 버튼을 클릭하면 실행되는 핵심 함수입니다.
   */
  const handleSaveDocument = async () => {
    // 1. 에디터의 현재 내용을 JSON과 HTML 형식으로 가져옵니다.
    const { jsonContent, htmlContent } = getEditorContent();
    if (!jsonContent || !htmlContent) {
      console.error("Editor content is empty or invalid.");
      alert('에디터 내용이 비어있어 저장할 수 없습니다.');
      return;
    }

    // 2. 카테고리 정보 추출
    const primaryCategory = projectSettings.tags.find(tag => Object.keys(CATEGORIES).includes(tag));
    const secondaryCategory = projectSettings.tags.find(tag => primaryCategory && CATEGORIES[primaryCategory]?.includes(tag));

    // 3. 판매 방식(saleType)에 따라 API URL과 전송할 데이터(payload)를 결정합니다.
    let apiUrl = '';
    let payload = {};
    const method = id ? 'PUT' : 'POST'; // id가 있으면 수정(PUT), 없으면 생성(POST)

    if (projectSettings.saleType === 'sale') {
      apiUrl = id ? `/api/products/${id}` : '/api/products';
      payload = {
        name: projectSettings.title,
        tiptapJson: JSON.stringify(jsonContent),
        description: htmlContent, // HTML 컨텐츠를 description으로 사용
        price: projectSettings.salePrice,
        imageUrl: projectSettings.coverImage,
        primaryCategory: primaryCategory,
        secondaryCategory: secondaryCategory,
        workDescription: projectSettings.workDescription,
        fontFamily: editorStyles.fontFamily,
        backgroundColor: editorStyles.backgroundColor,
        tags: projectSettings.tags, // 모든 태그 포함
      };
    } else if (projectSettings.saleType === 'auction') {
      apiUrl = id ? `/api/auctions/${id}` : '/api/auctions';
      // 경매 종료 시간 계산 (예: 7일을 더함)
      const auctionDurationMap = { '1일': 1, '3일': 3, '7일': 7 };
      const durationInDays = auctionDurationMap[projectSettings.auctionDuration] || 0;
      const auctionEndTime = new Date();
      auctionEndTime.setDate(auctionEndTime.getDate() + durationInDays);

      payload = {
        name: projectSettings.title,
        tiptapJson: JSON.stringify(jsonContent),
        description: htmlContent,
        startBidPrice: projectSettings.startBidPrice,
        buyNowPrice: projectSettings.buyNowPrice,
        auctionEndTime: auctionEndTime.toISOString(),
        imageUrl: projectSettings.coverImage,
        primaryCategory: primaryCategory,
        secondaryCategory: secondaryCategory,
        workDescription: projectSettings.workDescription,
        fontFamily: editorStyles.fontFamily,
        backgroundColor: editorStyles.backgroundColor,
        tags: projectSettings.tags, // 모든 태그 포함
      };
    } else {
      alert('판매 방식을 선택해주세요.');
      return;
    }

    // 4. 서버로 데이터 전송 (fetch API 사용)
    try {
      const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      const headers = {
        'Content-Type': 'application/json',
      };

      if (csrfHeader && csrfToken) {
        headers[csrfHeader] = csrfToken;
      }

      const response = await fetch(apiUrl, {
        method: method,
        headers: headers,
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        // 서버 응답이 실패했을 경우, 에러 메시지를 좀 더 자세히 보여줍니다.
        const errorText = await response.text();
        console.error('Server error response:', errorText);
        throw new Error(`서버 에러: ${response.status} ${response.statusText}`);
      }

      const savedId = await response.json();
      console.log('저장 성공! ID:', savedId);

      // 5. 저장 성공 후 결과 페이지로 이동합니다.
      const resultUrl = projectSettings.saleType === 'sale'
        ? `/products/${savedId}`
        : `/auctions/${savedId}`;
      window.location.href = resultUrl;

    } catch (error) {
      console.error('저장 실패:', error);
      alert('문서 저장에 실패했습니다. 콘솔을 확인해주세요.');
    }
  };

  /**
   * 에디터 스타일 모달에서 스타일을 변경했을 때 실행되는 이벤트 핸들러입니다.
   * @param {object} newStyles - 새로 적용될 스타일 객체
   */
  const handleStyleChange = (newStyles) => {
    setEditorStyles(newStyles); // App 컴포넌트의 에디터 스타일 상태를 업데이트합니다.

    // Google Fonts 로딩 (스타일 변경 시)
    if (newStyles.fontFamily) {
      const selectedFont = GOOGLE_FONTS.find(f => f.family === newStyles.fontFamily);
      if (selectedFont) {
        console.log('Loading Google Font for style change:', selectedFont.name);
        loadGoogleFont(selectedFont.name);
      }
    }
  };

  /**
   * 에디터 스타일 모달을 닫을 때 실행되는 이벤트 핸들러입니다.
   */
  const handleStylesModalClose = () => {
    setIsStylesModalOpen(false); // 모달을 닫습니다.
  };

  // 스페이서 생성 요청 핸들러 (사이드바에서 호출)
  const handleRequestSpacerCreation = () => {
    setSpacerModalConfig({
      currentHeight: '2rem', // 생성 시 기본 높이
      onSave: (newHeight) => {
        editor.chain().focus().setSpacer({ height: newHeight }).run();
        setIsSpacerModalOpen(false);
      },
    });
    setIsSpacerModalOpen(true);
  };

  //----------------------------------------------------------------
  // 5. JSX 렌더링
  // 이 컴포넌트가 화면에 어떻게 보일지를 정의하는 부분입니다. (HTML과 유사)
  // `return (...)` 안에 있는 코드를 JSX라고 부릅니다. 자바스크립트 안에 HTML과 유사한 문법을 사용할 수 있게 해줍니다.
  // React는 이 JSX 코드를 실제 웹 페이지의 HTML 요소로 변환하여 화면에 보여줍니다.
  //----------------------------------------------------------------
  return (
    // 최상위 div 요소입니다. React 컴포넌트는 보통 하나의 최상위 요소로 모든 것을 감싸야 합니다.
    <div className="app-container">
      <div className="main-content">
        {/* 에디터 본문 영역 */}
        {/* style={editorStyles} 처럼 중괄호 안에 자바스크립트 객체를 넣어 동적으로 스타일을 적용할 수 있습니다. */}
        <div className="editor-container" style={editorStyles}>
          {/* editor가 존재할 때만 BubbleMenuComponent를 렌더링합니다. (조건부 렌더링) */}
          {editor && (
            <Suspense fallback={<div>메뉴 로딩 중...</div>}>
              <BubbleMenuComponent editor={editor} />
            </Suspense>
          )}
          {/* Tiptap 에디터의 내용이 실제로 렌더링되는 컴포넌트입니다. */}
          <EditorContent editor={editor} />
        </div>

        {/* 사이드바 UI. 필요한 함수와 상태를 'props'라는 이름으로 자식 컴포넌트에 전달합니다. */}
        {/* 예를 들어, onEmbedClick={...}는 Sidebar 컴포넌트에게 onEmbedClick이라는 이름으로 함수를 전달하는 것입니다. */}
        <Suspense fallback={<div>사이드바 로딩 중...</div>}>
          <Sidebar
            editor={editor}
            onEmbedClick={() => setIsEmbedModalOpen(true)}
            onImageAdd={() => setIsImageModalOpen(true)}
            onVideoAdd={() => setIsVideoModalOpen(true)}
            onAudioAdd={() => setIsAudioModalOpen(true)}
            onStylesClick={() => setIsStylesModalOpen(true)}
            onSettingsClick={() => setIsSettingsModalOpen(true)}
            onPhotoGridClick={() => setIsPhotoGridModalOpen(true)}
            onSpacerAdd={handleRequestSpacerCreation} // 스페이서 생성 요청 함수 전달
            onPreviewClick={handlePreviewClick}
            onSaveClick={handleSaveDocument}
          />
        </Suspense>
      </div>

      {/* 조건부 렌더링: 각 모달의 'isOpen' 상태가 true일 때만 화면에 나타납니다. */}
      {/* `isImageModalOpen && (...)` 구문은 isImageModalOpen이 true이면 `(...)` 안의 JSX를 렌더링하고, false이면 아무것도 렌더링하지 않습니다. */}
      <div className="modals">
        {isImageModalOpen && (
          <Suspense fallback={<div>이미지 업로드 모달 로딩 중...</div>}>
            <ImageUploadModal onClose={() => setIsImageModalOpen(false)} onImageAdd={handleImageAdd} />
          </Suspense>
        )}
        {isVideoModalOpen && (
          <Suspense fallback={<div>비디오 업로드 모달 로딩 중...</div>}>
            <VideoUploadModal onClose={() => setIsVideoModalOpen(false)} onVideoAdd={handleVideoAdd} />
          </Suspense>
        )}
        {isAudioModalOpen && (
          <Suspense fallback={<div>오디오 업로드 모달 로딩 중...</div>}>
            <AudioUploadModal onClose={() => setIsAudioModalOpen(false)} onAudioAdd={handleAudioAdd} />
          </Suspense>
        )}
        {isEmbedModalOpen && (
          <Suspense fallback={<div>임베드 모달 로딩 중...</div>}>
            <EmbedModal onClose={() => setIsEmbedModalOpen(false)} onEmbed={handleEmbed} />
          </Suspense>
        )}
        {isSettingsModalOpen && (
          <Suspense fallback={<div>설정 모달 로딩 중...</div>}>
            <SettingsModal
              isOpen={isSettingsModalOpen}
              onClose={() => setIsSettingsModalOpen(false)}
              onSave={handleSettingsSave}
              initialSettings={projectSettings}
            />
          </Suspense>
        )}
        {isStylesModalOpen && (
          <Suspense fallback={<div>스타일 모달 로딩 중...</div>}>
            <StylesModal isOpen={isStylesModalOpen} onClose={handleStylesModalClose} onStyleChange={handleStyleChange} currentStyles={editorStyles} />
          </Suspense>
        )}
        {isPhotoGridModalOpen && (
          <Suspense fallback={<div>포토 그리드 모달 로딩 중...</div>}>
            <PhotoGridModal
              onClose={() => setIsPhotoGridModalOpen(false)}
              onGridCreate={handleCreateGrid}
            />
          </Suspense>
        )}
        {isPreviewModalOpen && (
          <Suspense fallback={<div>미리보기 모달 로딩 중...</div>}>
            <PreviewModal isOpen={isPreviewModalOpen} onClose={() => setIsPreviewModalOpen(false)} editorContent={getEditorContent().htmlContent} styles={editorStyles} />
          </Suspense>
        )}
        {isSpacerModalOpen && (
          <Suspense fallback={<div>스페이서 모달 로딩 중...</div>}>
            <SpacerModal isOpen={isSpacerModalOpen} onClose={() => setIsSpacerModalOpen(false)} onSave={spacerModalConfig.onSave} currentHeight={spacerModalConfig.currentHeight} />
          </Suspense>
        )}
      </div>
    </div>
  );
}

// `export default App;`은 이 파일의 App 컴포넌트를 다른 파일에서 import하여 사용할 수 있도록 내보내는 역할을 합니다.
export default App;
