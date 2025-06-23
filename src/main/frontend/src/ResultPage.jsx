import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import './ResultPage.css';
import './PaywallComponent.css';

const ResultPage = () => {
    const { id } = useParams();
    const [editorData, setEditorData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDocument = async () => {
            try {
                const response = await fetch(`/editor/api/documents/${id}`);
                if (!response.ok) {
                    throw new Error('문서를 불러오는 데 실패했습니다.');
                }
                const data = await response.json();
                setEditorData(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchDocument();
    }, [id]);

    if (loading) {
        return <div className="message-container">로딩 중...</div>;
    }

    if (error) {
        return <div className="message-container">에러: {error}</div>;
    }

    if (!editorData || typeof editorData !== 'object') {
        return <div className="container">데이터가 없습니다.</div>;
    }

    return (
        <div className="result-container">
            <header className="result-header">
                <div className="cover-image-container">
                   {editorData.coverImage && <img src={editorData.coverImage} alt={editorData.title} />}
                </div>
                <h1>{editorData.title}</h1>
                <div className="tags-container">
                    {editorData.tags && editorData.tags.map(tag => (
                        <span key={tag} className="tag">{tag}</span>
                    ))}
                </div>
                 <Link to={`/editor/${id}`} className="back-to-editor-link">에디터로 돌아가기</Link>
            </header>
            <main className="result-content">
                <div dangerouslySetInnerHTML={{ __html: editorData.htmlBackup }} />
            </main>
        </div>
    );
};

export default ResultPage; 