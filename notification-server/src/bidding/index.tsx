import React from 'react';
import ReactDOM from 'react-dom/client';
import BiddingComponent from './BiddingComponent'; // 같은 폴더에 있는 컴포넌트를 가져옵니다.
// import './bidding.css'; // 필요하다면 이 컴포넌트만을 위한 CSS 파일을 만들 수 있습니다.

// TypeScript에서 window 객체에 새로운 속성을 추가하기 위한 선언
// 이 파일 상단이나, 프로젝트 전역 타입 정의 파일(e.g., global.d.ts)에 위치시키면 됩니다.
declare global {
  interface Window {
    biddingAppData: {
      auctionId: number;
      initialHighestBid: number;
      buyNowPrice: number | null;
      auctionEndTime: string;
      authToken: string;
    };
  }
}

// Thymeleaf가 window 객체에 심어놓은 데이터를 가져옵니다.
// 만약 데이터가 없다면, 오류 방지를 위해 기본값을 사용합니다.
const appData = window.biddingAppData || {
    auctionId: 0,
    initialHighestBid: 0,
    buyNowPrice: null,
    auctionEndTime: '',
    authToken: ''
};

// Thymeleaf HTML에 있는 <div id="react-bidding-root">를 찾습니다.
const container = document.getElementById('react-bidding-root');

// 컨테이너가 존재할 경우에만 React 앱을 렌더링합니다.
if (container) {
  const root = ReactDOM.createRoot(container);
  root.render(
    <React.StrictMode>
      {/* 가져온 데이터를 props로 BiddingComponent에 전달합니다. */}
      <BiddingComponent
        auctionId={appData.auctionId}
        initialHighestBid={appData.initialHighestBid}
        buyNowPrice={appData.buyNowPrice}
        auctionEndTime={appData.auctionEndTime}
        authToken={appData.authToken}
      />
    </React.StrictMode>
  );
} else {
    console.error('React 주둔지("#react-bidding-root")를 찾을 수 없습니다.');
}