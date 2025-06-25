import React, { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import './ResultPage.css';
import './PaywallComponent.css';

const ResultPage = () => {
    const { id } = useParams();
    const location = useLocation();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 현재 경로에 따라 API 엔드포인트와 타입 결정
    const getApiInfo = () => {
        const path = location.pathname;
        if (path.includes('/result/auction/')) {
            return {
                endpoint: `/api/auctions/${id}`,
                type: 'auction',
                title: '경매 결과'
            };
        } else if (path.includes('/result/product/')) {
            return {
                endpoint: `/api/products/${id}`,
                type: 'product',
                title: '상품 결과'
            };
        } else {
            return {
                endpoint: `/editor/api/documents/${id}`,
                type: 'editor',
                title: '문서 결과'
            };
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                const { endpoint } = getApiInfo();
                console.log('Fetching data from:', endpoint);

                const response = await fetch(endpoint);
                if (!response.ok) {
                    throw new Error(`데이터를 불러오는 데 실패했습니다. (${response.status})`);
                }
                const fetchedData = await response.json();
                console.log('Fetched data:', fetchedData);
                setData(fetchedData);
            } catch (err) {
                console.error('Error fetching data:', err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id, location.pathname]);

    if (loading) {
        return <div className="message-container">로딩 중...</div>;
    }

    if (error) {
        return <div className="message-container">에러: {error}</div>;
    }

    if (!data || typeof data !== 'object') {
        return <div className="container">데이터가 없습니다.</div>;
    }

    const { type, title } = getApiInfo();

    return (
        <div className="result-container">
            <header className="result-header">
                <div className="cover-image-container">
                    {data.imageUrl && <img src={data.imageUrl} alt={data.name || data.title} />}
                </div>
                <h1>{data.name || data.title}</h1>
                <h2>{title}</h2>

                {/* 경매/상품 정보 표시 */}
                {type === 'auction' && (
                    <div className="auction-info">
                        <p>경매 기간: {data.auctionDuration}일</p>
                        <p>시작 입찰가: {data.startBidPrice?.toLocaleString()}원</p>
                        <p>즉시 입찰가: {data.buyNowPrice?.toLocaleString()}원</p>
                    </div>
                )}

                {type === 'product' && (
                    <div className="product-info">
                        <p>판매가: {data.price?.toLocaleString()}원</p>
                    </div>
                )}

                <div className="category-info">
                    {data.primaryCategory && <span className="tag primary">{data.primaryCategory}</span>}
                    {data.secondaryCategory && <span className="tag secondary">{data.secondaryCategory}</span>}
                </div>

                <Link to="/" className="back-to-editor-link">홈으로 돌아가기</Link>
            </header>

            <main className="result-content">
                <div
                    dangerouslySetInnerHTML={{
                        __html: data.description || data.htmlBackup
                    }}
                    style={{
                        backgroundColor: data.backgroundColor,
                        fontFamily: data.fontFamily
                    }}
                />
            </main>
        </div>
    );
};

export default ResultPage; 