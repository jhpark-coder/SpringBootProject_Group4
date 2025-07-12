import React, { useState, useEffect, useRef } from 'react';
import { io } from 'socket.io-client';

// 컴포넌트가 부모(App.jsx)로부터 props를 통해 데이터를 받습니다.
const BiddingComponent = ({
  auctionId,
  initialHighestBid,
  buyNowPrice,
  auctionEndTime,
  authToken
}) => {
  // 1. 상태(State) 관리
  // TypeScript의 타입 선언(<number>, <string> 등)을 모두 제거합니다.
  const [highestBid, setHighestBid] = useState(initialHighestBid);
  const [bidAmount, setBidAmount] = useState('');
  const [message, setMessage] = useState('');
  const [isAuctionEnded, setIsAuctionEnded] = useState(false);
  const socketRef = useRef(null);

  // 2. useEffect: 컴포넌트가 마운트될 때 실행되는 핵심 로직
  useEffect(() => {
    // 경매 종료 시간 확인
    if (new Date(auctionEndTime) < new Date()) {
      setIsAuctionEnded(true);
      setMessage('경매가 종료되었습니다.');
      return; // 더 이상 진행하지 않고 함수를 종료합니다.
    }

    // --- Socket.IO 연결 설정 ---
    // NestJS 서버의 'bidding' 네임스페이스에 접속합니다.
    const socket = io('http://localhost:3000/bidding', {
      reconnection: true, // 자동 재연결 활성화
      // auth 객체를 통해 서버로 인증 정보를 전달합니다.
      auth: {
        token: authToken,
      },
    });
    socketRef.current = socket;

    // --- 이벤트 수신(리스너) 설정 ---

    socket.on('connect', () => {
      console.log(`Socket.IO 연결 성공: ${socket.id}`);
      setMessage('입찰 준비 완료');
    });

    socket.on('bidding_update', (data) => {
      if (data.auctionId === auctionId) {
        if (data.success && data.newHighestBid) {
          setHighestBid(data.newHighestBid);
        }
        setMessage(data.message);
      }
    });

    socket.on('disconnect', () => {
      console.log('Socket.IO 연결 끊김');
      setMessage('서버와의 연결이 끊겼습니다.');
    });

    // 컴포넌트가 사라질 때 실행될 정리(cleanup) 함수
    return () => {
      if (socketRef.current) {
        socketRef.current.disconnect();
        socketRef.current = null;
      }
    };
  }, [auctionId, auctionEndTime, authToken]); // 의존성 배열은 그대로 유지합니다.

  // 3. 이벤트 핸들러: 입찰 버튼 클릭 시
  const handlePlaceBid = () => {
    const amount = parseInt(bidAmount, 10);

    // 클라이언트 측 유효성 검사
    if (isNaN(amount) || amount <= 0) {
      setMessage('올바른 입찰 금액을 입력하세요.');
      return;
    }
    if (amount <= highestBid) {
      setMessage('입찰가는 현재 최고가보다 높아야 합니다.');
      return;
    }
    if (buyNowPrice && amount > buyNowPrice) {
      setMessage('입찰가는 즉시 구매가를 초과할 수 없습니다.');
      return;
    }
    if (!socketRef.current) {
      setMessage('서버에 연결되지 않았습니다.');
      return;
    }

    setMessage('입찰 요청 중...');

    // 'placeBid' 이벤트를 서버(NestJS)로 전송(emit)
    socketRef.current.emit('placeBid', {
      auctionId,
      amount,
    });

    setBidAmount(''); // 입력 필드 초기화
  };

  // 4. JSX: 화면에 그려질 내용

  // 경매가 종료되었을 때 보여줄 화면
  if (isAuctionEnded) {
    return (
      <div className="bidding-container ended" style={{ padding: '20px', border: '1px solid #ccc', borderRadius: '8px', textAlign: 'center' }}>
        <h4>경매 종료</h4>
        <p>최종 입찰가: {highestBid.toLocaleString()}원</p>
      </div>
    );
  }

  // 경매가 진행 중일 때 보여줄 화면
  return (
    <div className="bidding-container" style={{ padding: '20px', border: '1px solid #007bff', borderRadius: '8px' }}>
      <h4 style={{ marginTop: 0 }}>실시간 입찰</h4>
      <div className="bid-info" style={{ marginBottom: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>현재 최고가</span>
        <strong style={{ fontSize: '1.5em' }}>{highestBid.toLocaleString()}원</strong>
      </div>
      <div className="bid-input-group" style={{ display: 'flex', gap: '10px' }}>
        <input
          type="number"
          value={bidAmount}
          onChange={(e) => setBidAmount(e.target.value)}
          placeholder="입찰 금액 입력"
          style={{ flexGrow: 1, padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
        />
        <button onClick={handlePlaceBid} style={{ padding: '10px 20px', border: 'none', background: '#007bff', color: 'white', borderRadius: '4px', cursor: 'pointer' }}>
          입찰하기
        </button>
      </div>
      <div className="bid-message" style={{ marginTop: '10px', minHeight: '20px', color: '#666' }}>
        {message}
      </div>
    </div>
  );
};

export default BiddingComponent;