import React, { useCallback, useEffect, useRef, useState } from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import { Responsive, WidthProvider } from 'react-grid-layout';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import _ from 'lodash';

const ResponsiveGridLayout = WidthProvider(Responsive);

const PhotoGridComponent = ({ node, updateAttributes, editor }) => {
    const { items = [], layout: initialLayout, savedLayouts = {} } = node.attrs;
    
    // 디버깅: 저장된 레이아웃 정보 확인
    console.log("PhotoGridComponent - node.attrs:", node.attrs);
    console.log("PhotoGridComponent - savedLayouts:", savedLayouts);
    const [isComponentMounted, setIsComponentMounted] = useState(false);
    const [currentLayout, setCurrentLayout] = useState([]);
    const [isDraggable, setIsDraggable] = useState(true);

    // 레이아웃에서 컬럼 수 추출
    const getColumnCount = (layout) => {
        if (typeof layout === 'string') {
            if (layout.includes('2-cols')) return 2;
            if (layout.includes('3-cols')) return 3;
            if (layout.includes('4-cols')) return 4;
            if (layout.includes('1-2-cols')) return 2; // 특별한 레이아웃
            if (layout.includes('2-1-cols')) return 2; // 특별한 레이아웃
        }
        return 2; // 기본값
    };

    const generateLayouts = useCallback(() => {
        const columnCount = getColumnCount(initialLayout);
        const cols = { lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 };
        const layouts = {};

        Object.keys(cols).forEach(breakpoint => {
            const breakpointCols = cols[breakpoint];
            const itemWidth = Math.floor(breakpointCols / columnCount);

            layouts[breakpoint] = items.map((item, i) => {
                // 저장된 레이아웃이 있으면 사용, 없으면 기본값 사용
                const savedItemLayout = savedLayouts[breakpoint] && savedLayouts[breakpoint].find(l => l.i === i.toString());
                
                if (savedItemLayout) {
                    return {
                        i: i.toString(),
                        x: savedItemLayout.x,
                        y: savedItemLayout.y,
                        w: savedItemLayout.w,
                        h: savedItemLayout.h,
                    };
                } else {
                    // 기본 레이아웃 생성
                    const x = (i % columnCount) * itemWidth;
                    const y = Math.floor(i / columnCount);
                    return {
                        i: i.toString(),
                        x: x,
                        y: y,
                        w: itemWidth,
                        h: 2,
                    };
                }
            });
        });
        return layouts;
    }, [items, initialLayout, savedLayouts]);

    const initialLayoutsRef = useRef(generateLayouts());

    useEffect(() => {
        setIsComponentMounted(true);
    }, []);

    // 레이아웃이 변경되면 다시 생성
    useEffect(() => {
        initialLayoutsRef.current = generateLayouts();
    }, [generateLayouts]);

    const debouncedUpdate = useCallback(_.debounce((newLayouts) => {
        // Only update if layout has actually changed
        if (!_.isEqual(newLayouts, savedLayouts)) {
            console.log("Updating attributes with new layouts:", newLayouts);
            updateAttributes({ savedLayouts: newLayouts });
        }
    }, 500), [updateAttributes, savedLayouts]);

    const handleLayoutChange = (layout, layouts) => {
        if (isComponentMounted) {
            console.log("Layout changed:", layout);
            console.log("All layouts:", layouts);
            setCurrentLayout(layout);
            debouncedUpdate(layouts);
        }
    };

    const handleMouseEnter = () => {
        if (editor.isEditable) {
            setIsDraggable(true);
        }
    };

    const handleMouseLeave = () => {
        if (editor.isEditable) {
            setIsDraggable(false);
        }
    };

    if (!items || items.length === 0) {
        return null;
    }

    return (
        <NodeViewWrapper
            className="photo-grid-node-view"
            contentEditable={false}
            suppressContentEditableWarning={true}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
        >
            <ResponsiveGridLayout
                className="layout"
                layouts={initialLayoutsRef.current}
                breakpoints={{ lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 }}
                cols={{ lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 }}
                rowHeight={100}
                onLayoutChange={handleLayoutChange}
                isDraggable={editor.isEditable}
                isResizable={editor.isEditable}
            >
                {items.map((item, index) => (
                    <div key={index.toString()} className="grid-item">
                        <img
                            src={item.src}
                            alt={item.alt}
                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            contentEditable={false}
                        />
                    </div>
                ))}
            </ResponsiveGridLayout>
        </NodeViewWrapper>
    );
};

export default PhotoGridComponent; 