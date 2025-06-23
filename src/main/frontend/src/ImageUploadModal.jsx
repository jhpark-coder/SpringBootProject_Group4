import React, { useState, useRef } from 'react';
import { X } from 'lucide-react';

/**
 * 이미지 추가를 위한 모달(팝업) 컴포넌트입니다.
 * URL로 이미지를 추가하거나, 컴퓨터에서 직접 파일을 업로드하는 두 가지 방법을 제공합니다.
 * @param {object} props - 부모 컴포넌트(App.jsx)로부터 전달받은 속성들
 * @param {function} props.onClose - 모달을 닫기 위해 호출하는 함수
 * @param {function} props.onImageAdd - 이미지 추가가 완료되었을 때, 이미지 정보를 부모에게 전달하기 위해 호출하는 함수
 */
const ImageUploadModal = ({ onClose, onImageAdd }) => {
  //-- 상태 관리(State) --//
  // 모달 내부에서 사용될 값들을 기억합니다.
  const [url, setUrl] = useState(''); // URL 입력 필드의 값
  const [alt, setAlt] = useState(''); // 대체 텍스트(alt) 입력 필드의 값
  const fileInputRef = useRef(null); // 숨겨진 파일 입력(input) 요소에 접근하기 위한 ref

  //-- 이벤트 핸들러 --//

  /**
   * 'Add Image' 버튼 클릭 시 실행됩니다. (URL로 추가)
   */
  const handleAddClick = () => {
    if (!url) {
      alert('Please enter a URL.');
      return;
    }
    // 부모(App.jsx)로부터 받은 onImageAdd 함수를 호출하여 데이터를 전달합니다.
    onImageAdd({ src: url, alt });
    // 모달을 닫습니다.
    onClose();
  };

  /**
   * 'Upload from Computer' 버튼으로 파일을 선택했을 때 실행됩니다.
   * @param {object} event - 파일 입력(input)의 변경 이벤트
   */
  const handleFileChange = async (event) => {
    const file = event.target.files?.[0];
    if (file) {
      try {
        // FormData는 파일과 같은 복잡한 데이터를 서버로 보낼 때 사용하는 형식입니다.
        const formData = new FormData();
        formData.append('file', file);

        // 백엔드의 파일 업로드 API를 호출합니다.
        const response = await fetch('/editor/api/upload', {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          const uploadedUrl = await response.text();
          // 업로드 성공 시, 반환된 URL로 이미지 정보를 구성하여 부모에게 전달합니다.
          onImageAdd({ src: uploadedUrl, alt: file.name }); // alt 텍스트는 우선 파일명으로 설정
          onClose();
        } else {
          alert('이미지 업로드에 실패했습니다.');
        }
      } catch (error) {
        console.error('업로드 오류:', error);
        alert('이미지 업로드 중 오류가 발생했습니다.');
      }
    }
  };

  /**
   * 'Upload from Computer' 버튼 클릭 시, 숨겨진 파일 입력(input)을 대신 클릭해줍니다.
   */
  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  //-- JSX 렌더링 --//
  return (
    // 모달의 배경. 클릭하면 모달이 닫힙니다.
    <div className="modal-overlay" onClick={onClose}>
      {/* 실제 모달 콘텐츠. 배경 클릭 이벤트가 전파되지 않도록 막습니다. */}
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button onClick={onClose} className="modal-close-button">
          <X size={24} />
        </button>
        <h3>Add an Image</h3>

        {/* 1. URL로 이미지 추가하는 섹션 */}
        <div className="modal-section">
          <label htmlFor="image-url">Embed from URL</label>
          <input
            id="image-url"
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://example.com/image.jpg"
          />
          <label htmlFor="alt-text">Alt Text</label>
          <input
            id="alt-text"
            type="text"
            value={alt}
            onChange={(e) => setAlt(e.target.value)}
            placeholder="Description of the image"
          />
          <button className="add-button" onClick={handleAddClick}>
            Add Image
          </button>
        </div>

        <div className="modal-divider">OR</div>

        {/* 2. 컴퓨터에서 파일 업로드하는 섹션 */}
        <div className="modal-section">
          <button className="upload-button" onClick={handleUploadClick}>
            Upload from Computer
          </button>
          {/* 이 input은 화면에는 보이지 않지만, 위 버튼을 통해 클릭됩니다. */}
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*"
            style={{ display: 'none' }}
          />
        </div>
      </div>
    </div>
  );
};

export default ImageUploadModal; 