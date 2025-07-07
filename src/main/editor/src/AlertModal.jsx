import React from 'react';
import './AlertModal.css';

/**
 * 커스텀 알림 모달 컴포넌트
 * 브라우저 기본 alert() 대신 사용하는 모달입니다.
 */
const AlertModal = ({ isOpen, message, onClose }) => {
    if (!isOpen) return null;

    return (
        <div className="alert-overlay" onClick={onClose}>
            <div className="alert-container" onClick={e => e.stopPropagation()}>
                <p className="alert-message">{message}</p>
                <button className="alert-button" onClick={onClose}>확인</button>
            </div>
        </div>
    );
};

export default AlertModal; 