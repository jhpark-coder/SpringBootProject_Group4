import {
  SubscribeMessage,
  WebSocketGateway,
  OnGatewayInit,
  WebSocketServer,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Logger, HttpService } from '@nestjs/common';
import { Server, Socket } from 'socket.io';
import { BiddingRequestDto } from './dto/bidding-request.dto';
import { firstValueFrom } from 'rxjs';

// 포트 및 CORS 설정: NestJS의 main.ts 또는 별도 설정 파일에서 관리됩니다.
// 예: app.enableCors({ origin: 'http://localhost:????' });
@WebSocketGateway({
  cors: {
    origin: '*', // 실제 운영 환경에서는 프론트엔드 도메인으로 제한해야 합니다.
  },
  namespace: 'bidding', // 채팅과 구분하기 위해 'bidding' 네임스페이스를 사용합니다.
})
export class BiddingGateway implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {

  // @WebSocketServer() 데코레이터는 서버 인스턴스에 대한 접근을 제공합니다.
  @WebSocketServer() server: Server;

  // NestJS의 로거와 HTTP 클라이언트를 주입받습니다.
  private logger: Logger = new Logger('BiddingGateway');
  private httpService: HttpService = new HttpService();

  /**
   * 클라이언트가 입찰 요청을 보낼 때 이 메소드가 호출됩니다.
   * @param client 클라이언트 소켓 정보
   * @param payload BiddingRequestDto (auctionId, amount)
   */
  @SubscribeMessage('placeBid')
  async handlePlaceBid(client: Socket, payload: BiddingRequestDto): Promise<void> {
    this.logger.log(`입찰 요청 받음: 클라이언트 ${client.id}, 데이터: ${JSON.stringify(payload)}`);

    // Spring Boot API 호출을 위한 준비
    const springBootApiUrl = `http://localhost:8080/api/auctions/${payload.auctionId}/bids`;
    const biddingData = { amount: payload.amount };

    // 중요: 클라이언트로부터 받은 JWT 토큰을 Spring Boot로 전달해야 합니다.
    // 실제 구현에서는 client.handshake.auth.token 등에서 토큰을 가져와야 합니다.
    const authToken = 'Bearer ' + client.handshake.headers.authorization; // 예시입니다. 실제 토큰 전달 방식에 맞게 수정 필요.

    try {
      // Spring Boot API에 입찰 처리 요청
      const response = await firstValueFrom(
        this.httpService.post(springBootApiUrl, biddingData, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': authToken,
          },
        }),
      );

      const responseData = response.data;
      this.logger.log(`Spring Boot 응답: ${JSON.stringify(responseData)}`);

      // Spring Boot의 응답에 따라 모든 클라이언트에게 결과를 방송(broadcast)
      const updatePayload = {
        success: responseData.success,
        message: responseData.message,
        auctionId: payload.auctionId,
        newHighestBid: responseData.newHighestBid,
        highestBidderName: responseData.highestBidderName,
      };

      // 'bidding_update' 이벤트를 모든 클라이언트에게 전송
      this.server.emit('bidding_update', updatePayload);

    } catch (error) {
      this.logger.error('Spring Boot API 호출 중 오류 발생', error.response?.data || error.message);

      // API 호출 실패 시, 요청한 클라이언트에게만 실패 메시지 전송
      const errorPayload = {
          success: false,
          message: error.response?.data?.message || '입찰 처리 중 서버 오류가 발생했습니다.',
          auctionId: payload.auctionId,
      };
      client.emit('bidding_update', errorPayload);
    }
  }

  // --- 연결 및 해제 로그 (디버깅용) ---

  afterInit(server: Server) {
    this.logger.log('BiddingGateway 초기화 완료!');
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`클라이언트 연결 해제: ${client.id}`);
  }

  handleConnection(client: Socket, ...args: any[]) {
    this.logger.log(`클라이언트 연결 성공: ${client.id}`);
    // 필요하다면 이곳에서 클라이언트에게 초기 데이터를 보내줄 수 있습니다.
  }
}