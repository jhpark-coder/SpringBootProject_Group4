import React, { useRef } from 'react';
import { Code, Image as ImageIcon, Video, Mic, Palette, LayoutGrid, Type, Settings } from 'lucide-react';

const Sidebar = ({ editor, onEmbed, onImageAdd, onStylesClick, onPhotoGridClick, onPreviewClick, onDebugClick, onSaveClick }) => {
    const videoInputRef = useRef(null);
    const audioInputRef = useRef(null);

    const handleEmbedClick = () => {
        const input = prompt('Paste the embed code from YouTube, Vimeo, etc. OR just the URL');
        if (input) {
            // Use a regular expression to find the src attribute
            const srcMatch = input.match(/src="([^"]+)"/);

            if (srcMatch && srcMatch[1]) {
                const url = srcMatch[1];
                const sanitizedUrl = url.replace(/&amp;/g, '&');
                onEmbed(sanitizedUrl);
            } else if (input.startsWith('http')) {
                onEmbed(input);
            } else {
                alert('Could not find a valid URL or embed code.');
            }
        }
    };

    const handleFileChange = async (event, nodeType) => {
        const file = event.target.files?.[0];
        if (file && editor) {
            try {
                // 파일을 서버에 업로드
                const formData = new FormData();
                formData.append('file', file);

                const response = await fetch('/editor/api/upload', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const uploadedUrl = await response.text();
                    // 업로드된 URL로 에디터에 추가
                    if (nodeType === 'video') {
                        editor.chain().focus().setVideo({ src: uploadedUrl }).run();
                    } else if (nodeType === 'audio') {
                        editor.chain().focus().setAudio({ src: uploadedUrl }).run();
                    }
                } else {
                    alert('파일 업로드에 실패했습니다.');
                }
            } catch (error) {
                console.error('업로드 오류:', error);
                alert('파일 업로드 중 오류가 발생했습니다.');
            }
            event.target.value = '';
        }
    };

    const handleVideoFileSelect = () => videoInputRef.current?.click();
    const handleAudioFileSelect = () => audioInputRef.current?.click();

    const handleAddText = () => {
        if (editor) {
            editor.chain().focus().insertContent('<p>New paragraph</p>').run();
        }
    };

    return (
        <div className="sidebar">
            <input
                type="file"
                ref={videoInputRef}
                onChange={(e) => handleFileChange(e, 'video')}
                style={{ display: 'none' }}
                accept="video/*"
            />
            <input
                type="file"
                ref={audioInputRef}
                onChange={(e) => handleFileChange(e, 'audio')}
                style={{ display: 'none' }}
                accept="audio/*"
            />
            <div className="sidebar-content">
                <div className="sidebar-section">
                    <h4 className="sidebar-title">ADD CONTENT</h4>
                    <div className="button-grid">
                        <button className="grid-button" onClick={handleEmbedClick}>
                            <Code size={20} />
                            <span>Embed</span>
                        </button>
                        <button className="grid-button" onClick={onImageAdd}>
                            <ImageIcon size={20} />
                            <span>Image</span>
                        </button>
                        <button className="grid-button" onClick={handleVideoFileSelect}>
                            <Video size={20} />
                            <span>Video</span>
                        </button>
                        <button className="grid-button" onClick={handleAudioFileSelect}>
                            <Mic size={20} />
                            <span>Audio</span>
                        </button>
                        <button className="grid-button" onClick={handleAddText}>
                            <Type size={20} />
                            <span>Text</span>
                        </button>
                        <button className="grid-button" onClick={onPhotoGridClick}>
                            <LayoutGrid size={20} />
                            <span>Photo Grid</span>
                        </button>
                    </div>
                </div>

                <div className="sidebar-section">
                    <h4 className="sidebar-title">STYLES</h4>
                    <div className="button-grid">
                        <button className="grid-button" onClick={onStylesClick}>
                            <Palette size={20} />
                            <span>Styles</span>
                        </button>
                        <button className="grid-button">
                            <Settings size={20} />
                            <span>Settings</span>
                        </button>
                    </div>
                </div>
            </div>

            <div className="sidebar-footer">
                <button
                    className="preview-button"
                    onClick={onPreviewClick}
                >
                    🔍 View a Preview
                </button>
                <button
                    className="update-button"
                    onClick={onSaveClick}
                >
                    Update Project
                </button>
                <button
                    className="preview-button"
                    onClick={onDebugClick}
                    style={{ marginTop: '0.5rem', fontSize: '0.8rem' }}
                >
                    🐛 Debug JSON
                </button>
            </div>
        </div>
    );
};

export default Sidebar; 