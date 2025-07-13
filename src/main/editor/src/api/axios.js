import axios from 'axios';

// Axios 인스턴스 생성
const apiClient = axios.create({
    // 백엔드 API의 기본 URL
    // Vite의 프록시 설정을 사용하므로, 상대 경로로도 충분합니다.
    baseURL: '/',
    // cross-origin 요청 시 인증 정보(쿠키 등)를 자동으로 포함
    withCredentials: true,

    // Axios의 내장 CSRF 보호 기능 설정
    // Spring Security의 기본 설정과 일치시킵니다.
    xsrfCookieName: 'XSRF-TOKEN', // 백엔드가 설정한 쿠키 이름
            xsrfHeaderName: 'X-CSRF-TOKEN', // 요청 시 보낼 헤더 이름
});

// apiClient.interceptors.request.use(...)
// 위와 같이 수동으로 만들었던 인터셉터는 더 이상 필요 없으므로 삭제합니다.
// Axios가 위 설정에 따라 자동으로 모든 것을 처리합니다.

export default apiClient; 