import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { X } from 'lucide-react';

const PhotoGridModal = ({ isOpen, onClose, onInsert }) => {
    const [files, setFiles] = useState([]);

    /*
    // 일단 이 부분을 주석 처리하여 세션 초기화 없이 테스트
    useEffect(() => {
        const fetchCsrfToken = async () => {
            try {
                await apiClient.get('/editor/api/init');
            } catch (error) {
                console.error('CSRF token initialization failed:', error);
            }
        };

        if (isOpen) {
            fetchCsrfToken();
        }
    }, [isOpen]);
    */

    const handleFileChange = async (event) => {
        const selectedFiles = Array.from(event.target.files);
        if (selectedFiles.length === 0) return;

        try {
            // 각 파일을 개별적으로 처리
            const processPromises = selectedFiles.map(async (file) => {
                return new Promise((resolve, reject) => {
                    const reader = new FileReader();
                    reader.onloadend = () => {
                        resolve(reader.result); // Base64 데이터 URL 반환
                    };
                    reader.onerror = () => {
                        reject(new Error(`Failed to read ${file.name}`));
                    };
                    reader.readAsDataURL(file);
                });
            });

            // 모든 파일이 처리될 때까지 대기
            const uploadedUrls = await Promise.all(processPromises);

            // 업로드된 URL들을 상태에 추가
            setFiles(prevFiles => [...prevFiles, ...uploadedUrls.map(url => ({ url, caption: '' }))]);

        } catch (error) {
            console.error('Error processing files:', error);
        }
    };

    const handleCaptionChange = (index, caption) => {
        const newFiles = [...files];
        newFiles[index].caption = caption;
        setFiles(newFiles);
    };

    const handleInsert = () => {
        onInsert(files);
        onClose();
    };

    const handleRemoveImage = (index) => {
        const newFiles = files.filter((_, i) => i !== index);
        setFiles(newFiles);
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="max-w-3xl">
                <DialogHeader>
                    <DialogTitle>포토 그리드 삽입</DialogTitle>
                </DialogHeader>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 max-h-[60vh] overflow-y-auto p-4">
                    {files.map((file, index) => (
                        <div key={index} className="relative group">
                            <img src={file.url} alt={`upload-preview ${index}`} className="w-full h-40 object-cover rounded-lg" />
                            <div className="absolute top-1 right-1">
                                <Button variant="destructive" size="icon" className="w-6 h-6" onClick={() => handleRemoveImage(index)}>
                                    <X className="h-4 w-4" />
                                </Button>
                            </div>
                            <input
                                type="text"
                                placeholder="캡션 입력..."
                                value={file.caption}
                                onChange={(e) => handleCaptionChange(index, e.target.value)}
                                className="mt-2 w-full p-2 border rounded"
                            />
                        </div>
                    ))}
                    <div className="w-full h-40 rounded-lg border-2 border-dashed flex items-center justify-center">
                        <input
                            type="file"
                            multiple
                            accept="image/*"
                            onChange={handleFileChange}
                            className="hidden"
                            id="photo-grid-file-upload"
                        />
                        <label htmlFor="photo-grid-file-upload" className="cursor-pointer">
                            <Button as="span">이미지 선택</Button>
                        </label>
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>취소</Button>
                    <Button onClick={handleInsert}>확인</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default PhotoGridModal; 