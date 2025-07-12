/**
 * @file App.jsx
 * @description 이 애플리케이션의 메인 컴포넌트입니다.
 *              Thymeleaf로부터 경매 초기 데이터를 받아,
 *              실시간 입찰 기능을 담당하는 BiddingComponent를 렌더링합니다.
 */

//================================================================================
// 0. 모듈 임포트
// React의 핵심 기능과, 우리가 만들 BiddingComponent를 가져옵니다.
//================================================================================
import React from 'react';
import BiddingComponent from './BiddingComponent'; // 입찰 UI 및 로직을 담을 파일 (다음 단계에서 생성할 것입니다)
import './App.css'; // 기본 스타일링

//================================================================================
// 메인 애플리케이션 컴포넌트
//================================================================================
function App() {

  //----------------------------------------------------------------
  // 1. 데이터 수신
  // Thymeleaf가 HTML의 window 객체에 심어놓은 초기 데이터를 가져옵니다.
  // 이 데이터는 React 앱이 시작하는 데 필요한 '군자금'과 같습니다.
  // 데이터가 없을 경우를 대비하여 빈 기본값을 설정합니다 (오류 방지).
  //----------------------------------------------------------------
  const appData = window.biddingAppData || {
    auctionId: null,
    initialHighestBid: 0,
    buyNowPrice: null,
    auctionEndTime: '',
    authToken: ''
  };

  //----------------------------------------------------------------
  // 2. 조건부 렌더링
  // 만약 필수 데이터인 auctionId가 없다면, 컴포넌트를 렌더링하지 않고
  // 사용자에게 안내 메시지를 보여줍니다. 이는 잘못된 접근을 막는 '방어벽'입니다.
  //----------------------------------------------------------------
  if (!appData.auctionId) {
    return (
      <div className="error-container" style={{ padding: '20px', color: 'red', border: '1px solid red', borderRadius: '5px' }}>
        경매 정보를 불러오는 데 실패했습니다. 올바른 경로로 접근했는지 확인해주세요.
      </div>
    );
  }

  //----------------------------------------------------------------
  // 3. JSX 렌더링
  // 핵심 임무: BiddingComponent를 화면에 그리고,
  //             props를 통해 수신한 초기 데이터를 전달(하달)합니다.
  //----------------------------------------------------------------
  return (
    <div className="App">
      <BiddingComponent
        auctionId={appData.auctionId}
        initialHighestBid={appData.initialHighestBid}
        buyNowPrice={appData.buyNowPrice}
        auctionEndTime={appData.auctionEndTime}
        authToken={appData.authToken}
      />
    </div>
  );
}

// 이 App 컴포넌트를 다른 파일(main.jsx)에서 사용할 수 있도록 내보냅니다.
export default App;

// window 객체에 커스텀 속성을 추가했음을 TypeScript나 Linter에게 알려주기 위한 선언입니다.
// JavaScript 환경에서는 필수는 아니지만, 코드의 명확성을 위해 포함하는 것이 좋습니다.
if (typeof window !== 'undefined') {
  window.biddingAppData = window.biddingAppData || {};
}