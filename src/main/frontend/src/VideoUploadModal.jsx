import React, { useState, useRef } from 'react';
import { X } from 'lucide-react';

/**
 * 비디오 추가를 위한 모달(팝업) 컴포넌트입니다.
 * URL로 비디오를 추가하거나, 컴퓨터에서 직접 파일을 업로드하는 두 가지 방법을 제공합니다.
 */
const VideoUploadModal = ({ onClose, onVideoAdd }) => {
  const [url, setUrl] = useState('');
  const fileInputRef = useRef(null);

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

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/editor/api/upload', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        const uploadedUrl = await response.text();
        onVideoAdd({ src: uploadedUrl });
        onClose();
      } else {
        alert('Video upload failed.');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('An error occurred during video upload.');
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button onClick={onClose} className="modal-close-button">
          <X size={24} />
        </button>
        <h3>Add a Video</h3>

        <div className="modal-section">
          <label htmlFor="video-url">Embed from URL</label>
          <input
            id="video-url"
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://example.com/video.mp4"
          />
          <button className="add-button" onClick={handleAddClick}>
            Add Video
          </button>
        </div>

        <div className="modal-divider">OR</div>

        <div className="modal-section">
          <button className="upload-button" onClick={handleUploadClick}>
            Upload from Computer
          </button>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="video/*"
            style={{ display: 'none' }}
          />
        </div>
      </div>
    </div>
  );
};

export default VideoUploadModal; 