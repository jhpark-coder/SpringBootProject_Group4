import React, { useRef } from 'react';
import {
  Code, Image as ImageIcon, Video, Mic, Palette, Grid as LayoutGrid, Type, Settings, DollarSign, Eye, Save, Link
} from 'lucide-react';

/**
 * 에디터의 사이드바 컴포넌트입니다.
 * 각종 콘텐츠 추가 버튼과 설정, 저장 버튼들이 위치합니다.
 * @param {object} props - 부모 컴포넌트(App.jsx)로부터 전달받은 속성들
 * @param {object} props.editor - Tiptap 에디터 인스턴스
 * @param {function} props.onEmbedClick - Embed 버튼 클릭 시 실행될 함수
 * @param {function} props.onImageAdd - Image 버튼 클릭 시 실행될 함수
 * @param {function} props.onVideoAdd - Video 버튼 클릭 시 실행될 함수
 * @param {function} props.onAudioAdd - Audio 버튼 클릭 시 실행될 함수
 * @param {function} props.onStylesClick - Styles 버튼 클릭 시 실행될 함수
 * @param {function} props.onSettingsClick - Settings 버튼 클릭 시 실행될 함수
 * @param {function} props.onPhotoGridClick - Photo Grid 버튼 클릭 시 실행될 함수
 * @param {function} props.onPreviewClick - Preview 버튼 클릭 시 실행될 함수
 * @param {function} props.onSaveClick - Save 버튼 클릭 시 실행될 함수
 */
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
}) => {
  // editor 객체가 아직 준비되지 않았으면 아무것도 렌더링하지 않습니다.
  if (!editor) {
    return null;
  }

  //-- JSX 렌더링 --//
  return (
    <div className="sidebar">
      <div className="sidebar-content">
        
        {/* 콘텐츠 추가 섹션 */}
        <div className="sidebar-section">
          <h4 className="sidebar-title">ADD CONTENT</h4>
          <div className="button-grid">
            <button className="grid-button" onClick={() => editor.chain().focus().insertCodeBlock({ language: 'auto' }).run()}>
              <Code size={20} />
              <span>Code</span>
            </button>
            <button className="grid-button" onClick={onEmbedClick}>
              <Link size={20} />
              <span>Embed</span>
            </button>
            <button className="grid-button" onClick={onImageAdd}>
              <ImageIcon size={20} />
              <span>Image</span>
            </button>
            <button className="grid-button" onClick={onVideoAdd}>
              <Video size={20} />
              <span>Video</span>
            </button>
            <button className="grid-button" onClick={onAudioAdd}>
              <Mic size={20} />
              <span>Audio</span>
            </button>
            <button className="grid-button" onClick={() => editor.chain().focus().insertContent('<p>여기에 텍스트를 입력하세요...</p>').run()}>
              <Type size={20} />
              <span>Text</span>
            </button>
            <button className="grid-button" onClick={onPhotoGridClick}>
              <LayoutGrid size={20} />
              <span>Photo Grid</span>
            </button>
          </div>
        </div>

        {/* 스타일 및 설정 섹션 */}
        <div className="sidebar-section">
          <h4 className="sidebar-title">STYLES</h4>
          <div className="button-grid">
            <button className="grid-button" onClick={onStylesClick}>
              <Palette size={20} />
              <span>Styles</span>
            </button>
            <button className="grid-button" onClick={() => editor.chain().focus().setPaywall().run()}>
              <DollarSign size={20} />
              <span>Paywall</span>
            </button>
            <button className="grid-button" onClick={onSettingsClick}>
              <Settings size={20} />
              <span>Settings</span>
            </button>
          </div>
        </div>

      </div>

      {/* 미리보기 및 저장 푸터 */}
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

        {/* 'JSON 디버그' 버튼: 클릭 시 개발자 콘솔에 현재 문서의 JSON 데이터를 출력합니다. */}
        <button className="preview-button" onClick={() => console.log(JSON.stringify(editor.getJSON(), null, 2))}>
          <Save size={14}/>
          <span>Debug JSON</span>
        </button>
      </div>
    </div>
  );
};

export default Sidebar; 