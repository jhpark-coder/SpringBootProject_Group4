import React, { useState, useRef } from 'react';
import { X, UploadCloud, Link as LinkIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';

/**
 * 이미지 추가를 위한 모달(팝업) UI를 담당하는 컴포넌트입니다.
 * URL로 이미지를 추가하거나, 컴퓨터에서 직접 파일을 업로드하는 두 가지 방법을 제공합니다.
 *
 * @param {object} props - 부모 컴포넌트(App.jsx)로부터 전달받은 속성(props) 객체입니다.
 * @param {function} props.onClose - 모달을 닫기 위해 부모가 전달해준 함수입니다. 이 함수를 호출하면 App.jsx의 `setIsImageModalOpen(false)`가 실행됩니다.
 * @param {function} props.onImageAdd - 이미지 추가가 완료되었을 때, 이미지 정보({src, alt})를 부모에게 전달하기 위해 호출하는 함수입니다.
 */
const ImageUploadModal = ({ onClose, onImageAdd }) => {
  //-- 상태 관리(State) --//
  // 모달 내부에서 사용될 값들을 기억합니다.
  const [url, setUrl] = useState(''); // URL 입력 필드의 값
  const [alt, setAlt] = useState(''); // 대체 텍스트(alt) 입력 필드의 값
  const [isUploading, setIsUploading] = useState(false); // 업로드 상태
  const [uploadProgress, setUploadProgress] = useState(0); // 업로드 진행률
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
   * 'Upload from Computer'를 통해 사용자가 파일을 선택했을 때 실행됩니다.
   * 서버 업로드 방식으로 변경했습니다.
   * @param {object} event - 파일 입력(input) 요소에서 발생한 변경 이벤트 객체
   */
  const handleFileChange = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // 이미지 파일 형식 확인
    const imageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!imageTypes.includes(file.type)) {
      alert('지원하지 않는 이미지 형식입니다.');
      return;
    }

    // 파일 크기 체크 (10MB 제한)
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      alert('이미지 파일 크기는 10MB를 초과할 수 없습니다.');
      return;
    }

    setIsUploading(true);
    setUploadProgress(0);

    try {
      const formData = new FormData();
      formData.append('file', file);

      // XMLHttpRequest를 사용하여 진행률 표시
      const xhr = new XMLHttpRequest();

      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) {
          const progress = Math.round((e.loaded / e.total) * 100);
          setUploadProgress(progress);
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          try {
            console.log('Raw response:', xhr.responseText);
            // 서버에서 문자열 URL을 반환하므로 직접 사용
            const imageUrl = xhr.responseText;
            console.log('Image URL:', imageUrl);

            if (imageUrl && imageUrl.trim() !== '' && imageUrl !== '[]') {
              // 상대 경로를 Spring Boot 서버의 전체 URL로 변환
              const fullImageUrl = `http://localhost:8080${imageUrl}`;
              console.log('Full image URL:', fullImageUrl);

              // Base64 데이터 URL을 이미지 소스로 사용
              onImageAdd({ src: fullImageUrl, alt: file.name });
              onClose();
            } else {
              console.error('Empty or invalid response from server:', imageUrl);
              alert('이미지 업로드 중 오류가 발생했습니다. 서버에서 유효한 URL을 반환하지 않았습니다.');
            }
          } catch (error) {
            console.error('응답 파싱 오류:', error);
            console.error('Response text:', xhr.responseText);
            alert('이미지 업로드 중 오류가 발생했습니다.');
          }
        } else {
          console.error('업로드 실패:', xhr.status, xhr.responseText);
          alert('이미지 업로드에 실패했습니다.');
        }
        setIsUploading(false);
        setUploadProgress(0);
      });

      xhr.addEventListener('error', () => {
        console.error('업로드 오류');
        alert('이미지 업로드 중 오류가 발생했습니다.');
        setIsUploading(false);
        setUploadProgress(0);
      });

      xhr.open('POST', '/editor/api/upload');
      xhr.send(formData);

    } catch (error) {
      console.error('파일 처리 오류:', error);
      alert('이미지 처리 중 오류가 발생했습니다.');
      setIsUploading(false);
      setUploadProgress(0);
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
    <div className="modal-overlay">
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add Image</h2>
        </div>
        <div className="modal-body">
          <div className="upload-tabs">
            {/* 1. URL로 이미지 추가하는 섹션 */}
            <div className="modal-section">
              <label htmlFor="image-url">Embed from URL</label>
              <div className="image-url-input">
                <LinkIcon className="input-icon" />
                <input
                  id="image-url"
                  type="text"
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder="https://example.com/image.jpg"
                />
              </div>
              <label htmlFor="alt-text">Alt Text</label>
              <input
                id="alt-text"
                type="text"
                value={alt}
                onChange={(e) => setAlt(e.target.value)}
                placeholder="Description of the image"
              />
              <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <Button onClick={handleAddClick}>Add Image</Button>
                <Button variant="outline" onClick={onClose}>Cancel</Button>
              </div>
            </div>

            <div className="modal-divider">OR</div>

            {/* 2. 컴퓨터에서 파일 업로드하는 섹션 */}
            <div className="modal-section">
              <button
                className="upload-button"
                onClick={handleUploadClick}
                disabled={isUploading}
              >
                {isUploading ? 'Uploading...' : 'Upload from Computer'}
              </button>
              {isUploading && (
                <div className="upload-progress">
                  <div className="progress-bar">
                    <div
                      className="progress-fill"
                      style={{ width: `${uploadProgress}%` }}
                    ></div>
                  </div>
                  <span>{uploadProgress}%</span>
                </div>
              )}
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
      </div>
    </div>
  );
};

export default ImageUploadModal; 