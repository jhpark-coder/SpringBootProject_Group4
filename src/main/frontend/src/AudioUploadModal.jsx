import React, { useState, useRef } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import './AudioUploadModal.css';

/**
 * 오디오 추가를 위한 모달(팝업) 컴포넌트입니다.
 * URL로 오디오를 추가하거나, 컴퓨터에서 직접 파일을 업로드하는 두 가지 방법을 제공합니다.
 */
const AudioUploadModal = ({ onClose, onAudioAdd }) => {
  const [url, setUrl] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const fileInputRef = useRef(null);

  const handleAddClick = () => {
    if (!url) {
      alert('Please enter an audio URL.');
      return;
    }
    onAudioAdd({ src: url });
    onClose();
  };

  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 파일 크기 체크 (50MB 제한)
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      alert('오디오 파일 크기는 50MB를 초과할 수 없습니다.');
      return;
    }

    // 파일 타입 체크
    if (!file.type.startsWith('audio/')) {
      alert('오디오 파일만 업로드 가능합니다.');
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
            const audioUrl = xhr.responseText;
            console.log('Audio URL:', audioUrl);

            if (audioUrl && audioUrl.trim() !== '' && audioUrl !== '[]') {
              // 상대 경로를 Spring Boot 서버의 전체 URL로 변환
              const fullAudioUrl = `http://localhost:8080${audioUrl}`;
              console.log('Full audio URL:', fullAudioUrl);
              onAudioAdd({ src: fullAudioUrl });
              onClose();
            } else {
              console.error('Empty response from server');
              alert('오디오 업로드 중 오류가 발생했습니다.');
            }
          } catch (error) {
            console.error('응답 파싱 오류:', error);
            console.error('Response text:', xhr.responseText);
            alert('오디오 업로드 중 오류가 발생했습니다.');
          }
        } else {
          console.error('업로드 실패:', xhr.status, xhr.responseText);
          alert('오디오 업로드에 실패했습니다.');
        }
        setIsUploading(false);
        setUploadProgress(0);
      });

      xhr.addEventListener('error', () => {
        console.error('업로드 오류');
        alert('오디오 업로드 중 오류가 발생했습니다.');
        setIsUploading(false);
        setUploadProgress(0);
      });

      xhr.open('POST', '/editor/api/upload');
      xhr.send(formData);

    } catch (error) {
      console.error('파일 처리 오류:', error);
      alert('오디오 처리 중 오류가 발생했습니다.');
      setIsUploading(false);
      setUploadProgress(0);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h3>Add an Audio</h3>

        <div className="modal-section">
          <label htmlFor="audio-url">Embed from URL</label>
          <input
            id="audio-url"
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://example.com/audio.mp3"
          />
        </div>

        <div className="modal-divider">OR</div>

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
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="audio/*"
            style={{ display: 'none' }}
          />
        </div>

        <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
          <Button onClick={handleUploadClick} disabled={isUploading}>Upload</Button>
          <Button variant="outline" onClick={onClose} disabled={isUploading}>Cancel</Button>
        </div>
      </div>
    </div>
  );
};

export default AudioUploadModal; 