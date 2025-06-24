import React, { useCallback, useEffect, useRef, useState } from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import { Responsive, WidthProvider } from 'react-grid-layout';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import _ from 'lodash';

const ResponsiveGridLayout = WidthProvider(Responsive);

const PhotoGridComponent = ({ node, updateAttributes, editor }) => {
    const { items = [], layout: initialLayout } = node.attrs;
    const [isComponentMounted, setIsComponentMounted] = useState(false);
    const [currentLayout, setCurrentLayout] = useState([]);
    const [isDraggable, setIsDraggable] = useState(true);

    const generateLayouts = useCallback(() => {
        const cols = { lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 };
        const layouts = {};
        Object.keys(cols).forEach(breakpoint => {
            layouts[breakpoint] = items.map((item, i) => ({
                i: i.toString(),
                x: (i * 2) % cols[breakpoint],
                y: Math.floor((i * 2) / cols[breakpoint]),
                w: 2,
                h: 2,
                ...item.layout
            }));
        });
        return layouts;
    }, [items]);

    const initialLayoutsRef = useRef(generateLayouts());

    useEffect(() => {
        setIsComponentMounted(true);
    }, []);

    const debouncedUpdate = useCallback(_.debounce((newLayout) => {
        // Only update if layout has actually changed
        if (!_.isEqual(newLayout, currentLayout)) {
            // console.log("Updating attributes with new layout:", newLayout);
            // updateAttributes({ layout: newLayout });
        }
    }, 500), [updateAttributes, currentLayout]);

    const handleLayoutChange = (layout, layouts) => {
        if (isComponentMounted) {
            // console.log("Layout changed:", layout);
            setCurrentLayout(layout);
            debouncedUpdate(layout);
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
                        <img src={item.src} alt={item.alt} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                ))}
            </ResponsiveGridLayout>
        </NodeViewWrapper>
    );
};

export default PhotoGridComponent; 