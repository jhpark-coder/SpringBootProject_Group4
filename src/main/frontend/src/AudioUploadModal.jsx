import React, { useState, useRef } from 'react';
import { X } from 'lucide-react';

/**
 * 오디오 추가를 위한 모달(팝업) 컴포넌트입니다.
 * URL로 오디오를 추가하거나, 컴퓨터에서 직접 파일을 업로드하는 두 가지 방법을 제공합니다.
 */
const AudioUploadModal = ({ onClose, onAudioAdd }) => {
  const [url, setUrl] = useState('');
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

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/editor/api/upload', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        const uploadedUrl = await response.text();
        onAudioAdd({ src: uploadedUrl });
        onClose();
      } else {
        alert('Audio upload failed.');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('An error occurred during audio upload.');
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
          <button className="add-button" onClick={handleAddClick}>
            Add Audio
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
            accept="audio/*"
            style={{ display: 'none' }}
          />
        </div>
      </div>
    </div>
  );
};

export default AudioUploadModal; 