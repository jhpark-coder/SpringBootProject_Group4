import React, { useState, useEffect, useRef } from 'react';
import { io, Socket } from 'socket.io-client';

// 컴포넌트가 부모로부터 받을 데이터의 타입 정의
interface BiddingProps {
  auctionId: number;
  initialHighestBid: number;
  buyNowPrice: number | null;
  auctionEndTime: string;
  authToken: string; // 사용자 인증 토큰 (JWT 등)
}

const BiddingComponent: React.FC<BiddingProps> = ({
  auctionId,
  initialHighestBid,
  buyNowPrice,
  auctionEndTime,
  authToken
}) => {
  // 1. 상태(State) 관리
  const [highestBid, setHighestBid] = useState<number>(initialHighestBid);
  const [bidAmount, setBidAmount] = useState<string>('');
  const [message, setMessage] = useState<string>('');
  const [isAuctionEnded, setIsAuctionEnded] = useState<boolean>(false);

  // 소켓 연결을 위한 Ref. 컴포넌트가 리렌더링되어도 소켓 연결이 유지되도록 합니다.
  const socketRef = useRef<Socket | null>(null);

  // 2. useEffect: 컴포넌트가 마운트/언마운트 될 때 실행되는 로직
  useEffect(() => {
    // 경매 종료 시간 확인
    if (new Date(auctionEndTime) < new Date()) {
      setIsAuctionEnded(true);
      setMessage('경매가 종료되었습니다.');
      return;
    }

    // --- Socket.IO 연결 설정 ---
    // NestJS 서버의 'bidding' 네임스페이스에 접속합니다.
    // 인증 토큰을 handshake 헤더에 담아 보냅니다.
    const socket = io('http://localhost:3000/bidding', {
      reconnection: true,
      auth: {
        token: authToken, // NestJS에서 이 토큰을 읽어 Spring Boot로 전달합니다.
      },
    });
    socketRef.current = socket;

    // --- 이벤트 수신(리스너) 설정 ---

    // 연결 성공 시
    socket.on('connect', () => {
      console.log(`Socket.IO 연결 성공: ${socket.id}`);
      setMessage('입찰 준비 완료');
    });

    // 'bidding_update' 이벤트를 받았을 때 (서버로부터의 전황 보고)
    socket.on('bidding_update', (data) => {
      // 이 업데이트가 현재 보고 있는 경매에 대한 것인지 확인
      if (data.auctionId === auctionId) {
        if (data.success && data.newHighestBid) {
          setHighestBid(data.newHighestBid); // 최고 입찰가 갱신
        }
        setMessage(data.message); // 서버가 보낸 메시지 표시
      }
    });

    // 연결 끊김 시
    socket.on('disconnect', () => {
      console.log('Socket.IO 연결 끊김');
      setMessage('서버와의 연결이 끊겼습니다.');
    });

    // 컴포넌트가 언마운트(사라질 때)될 때 소켓 연결을 정리합니다. (메모리 누수 방지)
    return () => {
      socket.disconnect();
      socketRef.current = null;
    };
  }, [auctionId, auctionEndTime, authToken]); // 이 값들이 바뀔 때만 useEffect가 다시 실행됩니다.

  // 3. 이벤트 핸들러 (사용자 행동에 대한 처리)

  // 입찰 버튼 클릭 시
  const handlePlaceBid = () => {
    const amount = parseInt(bidAmount, 10);

    // 유효성 검사 (클라이언트 측)
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

    // 입력 필드 초기화
    setBidAmount('');
  };

  // 4. JSX: 화면에 그려질 내용
  if (isAuctionEnded) {
    return (
      <div className="bidding-container ended">
        <h4>경매 종료</h4>
        <p>최종 입찰가: {highestBid.toLocaleString()}원</p>
      </div>
    );
  }

  return (
    <div className="bidding-container">
      <h4>실시간 입찰</h4>
      <div className="bid-info">
        <span>현재 최고가</span>
        <strong>{highestBid.toLocaleString()}원</strong>
      </div>
      <div className="bid-input-group">
        <input
          type="number"
          value={bidAmount}
          onChange={(e) => setBidAmount(e.target.value)}
          placeholder="입찰 금액 입력"
          className="bid-input"
        />
        <button onClick={handlePlaceBid} className="bid-button">
          입찰하기
        </button>
      </div>
      <div className="bid-message">
        {message}
      </div>
    </div>
  );
};

export default BiddingComponent;