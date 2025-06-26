import React, { useState } from 'react';
import axios from 'axios';

const VideoUploadModal = ({ isOpen, onClose, onVideoUpload }) => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState('');

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (file) {
            // 비디오 파일 형식 확인
            const videoTypes = ['video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/flv', 'video/webm'];
            if (!videoTypes.includes(file.type)) {
                setError('지원하지 않는 비디오 형식입니다.');
                return;
            }
            setSelectedFile(file);
            setError('');
        }
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            setError('파일을 선택해주세요.');
            return;
        }

        setUploading(true);
        setError('');

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);

            const response = await axios.post('/editor/api/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            if (response.status === 200) {
                const videoUrl = response.data;
                onVideoUpload(videoUrl);
                onClose();
                setSelectedFile(null);
            }
        } catch (error) {
            console.error('비디오 업로드 오류:', error);
            setError('비디오 업로드 중 오류가 발생했습니다.');
        } finally {
            setUploading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-96">
                <h2 className="text-xl font-bold mb-4">비디오 업로드</h2>

                <div className="mb-4">
                    <input
                        type="file"
                        accept="video/*"
                        onChange={handleFileSelect}
                        className="w-full p-2 border border-gray-300 rounded"
                    />
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

                <div className="flex justify-end space-x-2">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-gray-600 border border-gray-300 rounded hover:bg-gray-50"
                        disabled={uploading}
                    >
                        취소
                    </button>
                    <button
                        onClick={handleUpload}
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-400"
                        disabled={!selectedFile || uploading}
                    >
                        {uploading ? '업로드 중...' : '업로드'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default VideoUploadModal; 