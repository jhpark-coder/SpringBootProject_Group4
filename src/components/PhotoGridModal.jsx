import React, { useState } from 'react';
import axios from 'axios';

const PhotoGridModal = ({ isOpen, onClose, onPhotoUpload }) => {
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState('');

    const handleFileSelect = (e) => {
        const files = Array.from(e.target.files);
        const validFiles = files.filter(file => {
            const imageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
            if (!imageTypes.includes(file.type)) {
                setError('지원하지 않는 이미지 형식이 포함되어 있습니다.');
                return false;
            }
            return true;
        });

        if (validFiles.length > 0) {
            setSelectedFiles(validFiles);
            setError('');
        }
    };

    const handleUpload = async () => {
        if (selectedFiles.length === 0) {
            setError('파일을 선택해주세요.');
            return;
        }

        setUploading(true);
        setError('');

        try {
            const uploadedUrls = [];

            for (const file of selectedFiles) {
                const formData = new FormData();
                formData.append('file', file);

                const response = await axios.post('/editor/api/upload', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });

                if (response.status === 200) {
                    uploadedUrls.push(response.data);
                }
            }

            if (uploadedUrls.length > 0) {
                onPhotoUpload(uploadedUrls);
                onClose();
                setSelectedFiles([]);
            }
        } catch (error) {
            console.error('사진 업로드 오류:', error);
            setError('사진 업로드 중 오류가 발생했습니다.');
        } finally {
            setUploading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-96">
                <h2 className="text-xl font-bold mb-4">사진 그리드 업로드</h2>

                <div className="mb-4">
                    <input
                        type="file"
                        accept="image/*"
                        multiple
                        onChange={handleFileSelect}
                        className="w-full p-2 border border-gray-300 rounded"
                    />
                </div>

                {error && (
                    <div className="mb-4 p-2 bg-red-100 text-red-700 rounded">
                        {error}
                    </div>
                )}

                {selectedFiles.length > 0 && (
                    <div className="mb-4 p-2 bg-gray-100 rounded">
                        <p>선택된 파일: {selectedFiles.length}개</p>
                        {selectedFiles.map((file, index) => (
                            <p key={index} className="text-sm text-gray-600">
                                {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                            </p>
                        ))}
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
                        disabled={selectedFiles.length === 0 || uploading}
                    >
                        {uploading ? '업로드 중...' : '업로드'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PhotoGridModal; 