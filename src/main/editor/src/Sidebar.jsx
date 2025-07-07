//================================================================================
// 0. 모듈 임포트
//================================================================================
import React, { useRef } from 'react'; // React 라이브러리를 가져옵니다. JSX를 사용하기 위해 필수입니다.
import { // lucide-react 라이브러리에서 아이콘 컴포넌트들을 가져옵니다.
  Code, Image as ImageIcon, Video, Mic, Palette, Grid as LayoutGrid, Type, Settings, DollarSign, Eye, Save, Link, Minus, ArrowUpDown
} from 'lucide-react';

//================================================================================
// 1. 컴포넌트 Props 설명 주석
//================================================================================
/**
 * 에디터의 사이드바 UI를 담당하는 컴포넌트입니다.
 * 각종 콘텐츠 추가 버튼과 설정, 저장 버튼들이 이 컴포넌트에 포함됩니다.
 *
 * @param {object} props - 부모 컴포넌트(App.jsx)로부터 전달받은 속성(데이터나 함수)들의 묶음입니다.
 *                         리액트에서는 이렇게 부모가 자식에게 데이터를 전달하는 방식을 'props'라고 부릅니다.
 * @param {object} props.editor - Tiptap 에디터의 핵심 인스턴스입니다. 에디터를 조작하는 데 사용됩니다.
 * @param {function} props.onEmbedClick - 'Embed' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onImageAdd - 'Image' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onVideoAdd - 'Video' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onAudioAdd - 'Audio' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onStylesClick - 'Styles' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onSettingsClick - 'Settings' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onPhotoGridClick - 'Photo Grid' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onPreviewClick - 'Preview' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onSaveClick - 'Save' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 * @param {function} props.onSpacerAdd - 'Spacer' 버튼 클릭 시 App.jsx에서 실행될 함수입니다.
 */
//================================================================================
// 2. 사이드바 컴포넌트 정의
//================================================================================
// 'Sidebar'라는 이름의 함수 컴포넌트를 정의합니다.
// 매개변수로 'props' 객체를 받지만, 자바스크립트의 '구조 분해 할당' 문법을 사용하여
// props 객체 안의 각 속성(editor, onEmbedClick 등)을 바로 변수로 사용할 수 있게 합니다.
const Sidebar = ({
  editor,
  onEmbedClick,
  onImageAdd,
  onVideoAdd,
  onAudioAdd,
  onStylesClick,
  onSettingsClick,
  onPhotoGridClick,
  onPreviewClick,
  onSaveClick,
  onSpacerAdd,
}) => {
  // --- 방어 코드 (Defensive Code) ---
  // editor 객체가 아직 준비되지 않았을 수 있습니다. (예: 데이터 로딩 중)
  // 이 경우, 오류를 발생시키지 않고 아무것도 렌더링하지 않도록 `null`을 반환합니다.
  if (!editor) {
    return null;
  }

  //================================================================================
  // 3. JSX 렌더링
  // 이 컴포넌트가 화면에 어떻게 보일지를 JSX로 작성하여 반환합니다.
  //================================================================================
  return (
    // className은 HTML의 class 속성과 동일하며, CSS 스타일을 적용하기 위해 사용됩니다.
    <div className="sidebar">
      <div className="sidebar-content">

        {/* 콘텐츠 추가 섹션 */}
        <div className="sidebar-section">
          <h4 className="sidebar-title">ADD CONTENT</h4>
          <div className="button-grid">
            {/* 
              각 버튼에는 `onClick` 이벤트 핸들러가 연결되어 있습니다.
              버튼을 클릭하면 `onClick`에 지정된 함수가 실행됩니다.
            */}

            {/* 코드 블록 추가 버튼 */}
            {/* onClick 내부의 함수는 Tiptap 에디터 명령어를 직접 호출합니다. */}
            {/* `editor.chain().focus()...run()`은 Tiptap에서 여러 명령을 연결하여 실행하는 표준 방식입니다. */}
            <button className="grid-button" onClick={() => editor.chain().focus().insertCodeBlock({ language: 'auto' }).run()}>
              <Code size={20} /> {/* lucide-react에서 가져온 아이콘 컴포넌트 */}
              <span>Code</span>
            </button>

            {/* 임베드 버튼 */}
            {/* onClick에는 부모(App.jsx)로부터 props로 전달받은 `onEmbedClick` 함수를 연결합니다. */}
            {/* 이렇게 하면 버튼 클릭 시 App.jsx에 있는 `setIsEmbedModalOpen(true)`가 실행되어 모달이 열립니다. */}
            <button className="grid-button" onClick={onEmbedClick}>
              <Link size={20} />
              <span>Embed</span>
            </button>

            {/* 이미지 추가 버튼 */}
            <button className="grid-button" onClick={onImageAdd}>
              <ImageIcon size={20} />
              <span>Image</span>
            </button>

            {/* 포토 그리드 추가 버튼 */}
            <button className="grid-button" onClick={onPhotoGridClick}>
              <LayoutGrid size={20} />
              <span>Photo Grid</span>
            </button>

            {/* 비디오 추가 버튼 */}
            <button className="grid-button" onClick={onVideoAdd}>
              <Video size={20} />
              <span>Video</span>
            </button>

            {/* 오디오 추가 버튼 */}
            <button className="grid-button" onClick={onAudioAdd}>
              <Mic size={20} />
              <span>Audio</span>
            </button>

            {/* 텍스트 추가 버튼 */}
            {/* Tiptap의 `insertContent` 명령어로 새로운 단락(<p>)을 에디터에 직접 삽입합니다. */}
            <button className="grid-button" onClick={() => editor.chain().focus().insertContent('<p>여기에 텍스트를 입력하세요...</p>').run()}>
              <Type size={20} />
              <span>Text</span>
            </button>

            {/* 라인 추가 버튼 */}
            <button className="grid-button" onClick={() => editor.chain().focus().setHorizontalRule().run()}>
              <Minus size={20} />
              <span>Line</span>
            </button>

            {/* 공백 추가 버튼 */}
            <button className="grid-button" onClick={onSpacerAdd}>
              <ArrowUpDown size={20} />
              <span>Spacer</span>
            </button>

            {/* 유료 콘텐츠(Paywall) 영역 추가 버튼 */}
            <button className="grid-button" onClick={() => editor.chain().focus().setPaywall().run()}>
              <DollarSign size={20} />
              <span>Paywall</span>
            </button>
          </div>
        </div>

        {/* 스타일 및 설정 섹션 */}
        <div className="sidebar-section">
          <h4 className="sidebar-title">STYLES</h4>
          <div className="button-grid">
            {/* 스타일 변경 버튼 */}
            <button className="grid-button" onClick={onStylesClick}>
              <Palette size={20} />
              <span>Styles</span>
            </button>

            {/* 프로젝트 설정 버튼 */}
            <button className="grid-button" onClick={onSettingsClick}>
              <Settings size={20} />
              <span>Settings</span>
            </button>
          </div>
        </div>

      </div>

      {/* 사이드바 하단의 미리보기 및 저장 영역 */}
      <div className="sidebar-footer">
        {/* '미리보기' 버튼 */}
        <button
          className="preview-button"
          onClick={onPreviewClick}
        >
          <Eye size={18} />
          <span>Preview</span>
        </button>

        {/* 메인 저장/업데이트 버튼 */}
        <button onClick={onSaveClick} className="update-button">
          Project Submit
        </button>

        {/* 'JSON 디버그' 버튼: 클릭 시 개발자 콘솔에 현재 문서의 Tiptap JSON 데이터를 출력합니다. */}
        {/* 이는 개발 과정에서 에디터의 내부 데이터 구조를 확인할 때 유용합니다. */}
        <button className="preview-button" onClick={() => console.log(JSON.stringify(editor.getJSON(), null, 2))}>
          <Save size={14} />
          <span>Debug JSON</span>
        </button>
      </div>
    </div>
  );
};

// `export default Sidebar;`는 이 파일의 Sidebar 컴포넌트를 다른 파일에서 import하여 사용할 수 있도록 내보내는 역할을 합니다.
export default Sidebar; 