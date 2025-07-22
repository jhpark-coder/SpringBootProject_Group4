import React, { useState, useRef } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import './VideoUploadModal.css';

/**
 * 비디오 추가를 위한 모달(팝업) 컴포넌트입니다.
 * URL로 비디오를 추가하거나, 컴퓨터에서 직접 파일을 업로드하는 두 가지 방법을 제공합니다.
 */
const VideoUploadModal = ({ onClose, onVideoAdd }) => {
  const [url, setUrl] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const fileInputRef = useRef(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');

  const handleAddClick = () => {
    if (!url) {
      alert('Please enter a video URL.');
      return;
    }
    onVideoAdd({ src: url });
    onClose();
  };

  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 파일 크기 체크 (100MB 제한)
    const maxSize = 100 * 1024 * 1024; // 100MB
    if (file.size > maxSize) {
      alert('비디오 파일 크기는 100MB를 초과할 수 없습니다.');
      return;
    }

    // 파일 타입 체크
    if (!file.type.startsWith('video/')) {
      alert('비디오 파일만 업로드 가능합니다.');
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
            console.log('Response type:', typeof xhr.responseText);
            console.log('Response length:', xhr.responseText.length);

            // 서버에서 문자열 URL을 반환하므로 직접 사용
            const videoUrl = xhr.responseText;
            console.log('Video URL:', videoUrl);
            console.log('Video URL type:', typeof videoUrl);
            console.log('Video URL length:', videoUrl.length);

            if (videoUrl && videoUrl.trim() !== '' && videoUrl !== '[]') {
              console.log('Calling onVideoAdd with:', { src: videoUrl });
              // 상대 경로를 Spring Boot 서버의 전체 URL로 변환
              const fullVideoUrl = `${window.location.origin}${videoUrl}`;
              console.log('Full video URL:', fullVideoUrl);
              onVideoAdd({ src: fullVideoUrl });
              onClose();
            } else {
              console.error('Empty or invalid response from server:', videoUrl);
              alert('비디오 업로드 중 오류가 발생했습니다. 서버에서 유효한 URL을 반환하지 않았습니다.');
            }
          } catch (error) {
            console.error('응답 파싱 오류:', error);
            console.error('Response text:', xhr.responseText);
            alert('비디오 업로드 중 오류가 발생했습니다.');
          }
        } else {
          console.error('업로드 실패:', xhr.status, xhr.responseText);
          alert('비디오 업로드에 실패했습니다.');
        }
        setIsUploading(false);
        setUploadProgress(0);
      });

      xhr.addEventListener('error', () => {
        console.error('업로드 오류');
        alert('비디오 업로드 중 오류가 발생했습니다.');
        setIsUploading(false);
        setUploadProgress(0);
      });

      xhr.open('POST', '/editor/api/upload');
      xhr.send(formData);

    } catch (error) {
      console.error('파일 처리 오류:', error);
      alert('비디오 처리 중 오류가 발생했습니다.');
      setIsUploading(false);
      setUploadProgress(0);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 파일 크기 체크 (100MB 제한)
    const maxSize = 100 * 1024 * 1024; // 100MB
    if (file.size > maxSize) {
      alert('비디오 파일 크기는 100MB를 초과할 수 없습니다.');
      return;
    }

    // 파일 타입 체크
    if (!file.type.startsWith('video/')) {
      alert('비디오 파일만 업로드 가능합니다.');
      return;
    }

    setSelectedFile(file);
    setError('');
  };

  const handleUpload = () => {
    if (!selectedFile) {
      setError('Please select a video file.');
      return;
    }
    handleFileChange({ target: { files: [selectedFile] } });
  };

  return (
    <div className="modal-overlay fancy-modal-overlay" onClick={onClose}>
      <div className="modal-content fancy-modal-content animate-fadeIn" onClick={e => e.stopPropagation()}>
        <h2 className="fancy-modal-title">비디오 업로드</h2>
        <div className="modal-section" style={{ marginBottom: '1.2rem' }}>
          <label className="fancy-label">URL로 비디오 추가</label>
          <div className="fancy-url-input-group">
            <input
              type="url"
              placeholder="https://example.com/video.mp4"
              value={url}
              onChange={e => setUrl(e.target.value)}
              className="fancy-input"
            />
            <button
              onClick={handleAddClick}
              className="fancy-embed-btn"
              disabled={!url}
            >
              추가
            </button>
          </div>
        </div>
        <div className="modal-section">
          <label className="fancy-label">파일 업로드</label>
          <div className="fancy-file-upload-group" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <input
              type="file"
              accept="video/*"
              id="video-upload-input"
              style={{ display: 'none' }}
              onChange={handleFileSelect}
            />
            <label htmlFor="video-upload-input" className="fancy-embed-btn fancy-file-btn">
              파일 선택
            </label>
            {selectedFile && <span className="fancy-file-name">{selectedFile.name}</span>}
          </div>
        </div>
        {error && (
          <div className="mb-4 p-2 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}
        {selectedFile && (
          <div className="mb-4 p-2 bg-gray-100 rounded">
            <p>선택된 파일: {selectedFile.name}</p>
            <p>크기: {(selectedFile.size / 1024 / 1024).toFixed(2)} MB</p>
          </div>
        )}
        <div className="flex justify-end">
          <button
            onClick={handleUpload}
            className="fancy-embed-btn"
            disabled={!selectedFile || isUploading}
          >
            {isUploading ? '업로드 중...' : '업로드'}
          </button>
          <button
            onClick={onClose}
            className="fancy-embed-btn"
            disabled={isUploading}
          >
            취소
          </button>
        </div>
      </div>
    </div>
  );
};

export default VideoUploadModal; 