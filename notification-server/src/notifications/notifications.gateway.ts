import {
  WebSocketGateway,
  SubscribeMessage,
  MessageBody,
  OnGatewayInit,
  OnGatewayConnection,
  OnGatewayDisconnect,
  WebSocketServer,
  ConnectedSocket,
} from '@nestjs/websockets';
import { NotificationsService } from './notifications.service';
import { Server, Socket } from 'socket.io';
import { Logger } from '@nestjs/common';
import { CreateNotificationDto } from './dto/create-notification.dto';

@WebSocketGateway({
  cors: {
    origin: 'http://localhost:8080',
    credentials: true,
  },
})
export class NotificationsGateway
  implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer()
  server: Server;

  private logger: Logger = new Logger('NotificationsGateway');

  constructor(private readonly notificationsService: NotificationsService) {
    console.log('웹소켓 서버 초기화 완료');
  }

  afterInit(server: Server) {
    this.logger.log('웹소켓 서버 초기화 완료');
  }

  handleConnection(client: Socket, ...args: any[]) {
    this.logger.log(`클라이언트 연결: ${client.id}`);
    const userId = this.getUserIdFromSocket(client);
    const userRoles = this.getUserRolesFromSocket(client);

    if (userId) {
      client.join(String(userId));
      this.logger.log(`클라이언트 ${client.id}가 사용자 ${userId} 방에 참가했습니다.`);
    }

    // 사용자가 ADMIN 역할을 가지고 있으면 admin 방에도 참가
    if (userRoles && userRoles.includes('ROLE_ADMIN')) {
      client.join('admin');
      this.logger.log(`관리자 ${client.id}가 admin 방에 참가했습니다.`);
    }
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`클라이언트 연결 끊김: ${client.id}`);
  }

  @SubscribeMessage('createNotification')
  async create(@MessageBody() createNotificationDto: CreateNotificationDto) {
    const notification = await this.notificationsService.create(createNotificationDto);
    this.server.emit('newNotification', notification);
    return notification;
  }

  @SubscribeMessage('findAllNotifications')
  async findAll(@ConnectedSocket() client: Socket) {
    const userId = this.getUserIdFromSocket(client);
    const roles = this.getUserRolesFromSocket(client);

    if (!userId) {
      client.emit('notifications', { notifications: [] });
      return;
    }

    let notifications = this.notificationsService.findByUserId(userId);

    // 사용자가 관리자일 경우, ADMIN 카테고리의 모든 알림을 추가로 가져와 합침
    if (roles && roles.includes('ROLE_ADMIN')) {
      const adminNotifications = this.notificationsService.findByCategory('ADMIN');
      // 중복 방지를 위해 기존 목록에 없는 알림만 추가
      adminNotifications.forEach(adminNoti => {
        if (!notifications.some(userNoti => userNoti.id === adminNoti.id)) {
          notifications.push(adminNoti);
        }
      });
    }

    // 최신순으로 정렬
    notifications.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    client.emit('notifications', { notifications });
  }

  @SubscribeMessage('findOneNotification')
  async findOne(@MessageBody() id: number) {
    return this.notificationsService.findOne(id);
  }

  @SubscribeMessage('markAsRead')
  async markAsRead(
    @MessageBody() data: { id: number },
    @ConnectedSocket() client: Socket,
  ) {
    const userId = this.getUserIdFromSocket(client);
    if (!userId) return false;
    return this.notificationsService.markAsRead(data.id, userId);
  }

  @SubscribeMessage('markAllAsRead')
  async markAllAsRead(@ConnectedSocket() client: Socket) {
    const userId = this.getUserIdFromSocket(client);
    if (!userId) return 0;
    return this.notificationsService.markAllAsRead(userId);
  }

  /**
   * 특정 사용자에게 새 알림을 전송합니다.
   * @param userId 알림을 받을 사용자의 ID
   * @param notification 알림 객체
   */
  public sendNotificationToUser(userId: number, notification: any) {
    this.logger.log(`${userId}번 사용자에게 알림을 전송합니다.`);
    this.server.to(String(userId)).emit('newNotification', notification);
  }

  /**
   * 관리자 그룹에게 새 알림을 전송합니다.
   * @param notification 알림 객체
   */
  public sendNotificationToAdminGroup(notification: any) {
    this.logger.log('관리자 그룹에게 알림을 전송합니다.');
    this.server.to('admin').emit('newNotification', notification);
  }

  private getUserIdFromSocket(client: Socket): number | null {
    // 소켓 연결에서 사용자 ID를 추출하는 로직
    // 실제 구현에서는 토큰이나 세션에서 사용자 ID를 가져와야 합니다
    return client.handshake.auth.userId || null;
  }

  private getUserRolesFromSocket(client: Socket): string[] | null {
    // 소켓 연결에서 사용자 역할을 추출하는 로직
    return client.handshake.auth.roles || null;
  }
}
