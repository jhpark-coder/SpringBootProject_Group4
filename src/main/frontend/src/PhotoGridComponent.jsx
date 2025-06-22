import React, { useState, useEffect, useCallback, useRef } from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import GridLayout from 'react-grid-layout';

// react-grid-layout의 CSS를 import해야 합니다.
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';

const PhotoGridComponent = ({ node, updateAttributes }) => {
    const { layout: initialLayout, items } = node.attrs;
    const [layout, setLayout] = useState(initialLayout);
    const updateTimeoutRef = useRef(null);
    const lastUpdateRef = useRef(null);

    // 디바운싱된 업데이트 함수
    const debouncedUpdate = useCallback((newLayout) => {
        // 이전 업데이트와 동일한지 확인
        if (lastUpdateRef.current && JSON.stringify(lastUpdateRef.current) === JSON.stringify(newLayout)) {
            return;
        }

        // 기존 타이머 클리어
        if (updateTimeoutRef.current) {
            clearTimeout(updateTimeoutRef.current);
        }

        // 새로운 타이머 설정 (300ms 디바운스)
        updateTimeoutRef.current = setTimeout(() => {
            lastUpdateRef.current = newLayout;
            updateAttributes({ layout: newLayout });
        }, 300);
    }, [updateAttributes]);

    // 컴포넌트 언마운트 시 타이머 정리
    useEffect(() => {
        return () => {
            if (updateTimeoutRef.current) {
                clearTimeout(updateTimeoutRef.current);
            }
        };
    }, []);

    const onLayoutChange = (newLayout) => {
        setLayout(newLayout);
    };

    // 그리드 아이템이 없는 경우 렌더링하지 않습니다.
    if (!items || items.length === 0) {
        return null;
    }

    return (
        <NodeViewWrapper className="photo-grid-wrapper">
            <GridLayout
                layout={layout}
                onLayoutChange={onLayoutChange}
                cols={12} // 그리드를 12단으로 설정하여 유연성을 높입니다.
                rowHeight={30}
                width={1200} // 기본 너비를 설정합니다. 실제 너비는 CSS로 조절됩니다.
                className="photo-grid-layout"
                margin={[10, 10]} // 아이템 간의 간격
            >
                {items.map(item => (
                    <div key={item.id} className="grid-item">
                        <img src={item.src} alt={`grid item ${item.id}`} />
                    </div>
                ))}
            </GridLayout>
        </NodeViewWrapper>
    );
};

export default PhotoGridComponent; 