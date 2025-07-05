import { Controller, Get, Post, Param, Body } from '@nestjs/common';
import { NotificationsService, NotificationDto } from './notifications.service';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { NotificationsGateway } from './notifications.gateway';

@Controller('api/notifications')
export class NotificationsController {
  constructor(
    private readonly notificationsService: NotificationsService,
    private readonly notificationsGateway: NotificationsGateway,
  ) { }

  @Get('count')
  getUnreadNotificationCount(): { count: number } {
    // TODO: 실제 인증된 사용자 ID를 가져와야 함
    const userId = 1; // 임시로 하드코딩
    const count = this.notificationsService.getUnreadCount(userId);
    return { count };
  }

  @Get('list')
  getNotifications(): { notifications: NotificationDto[] } {
    // TODO: 실제 인증된 사용자 ID를 가져와야 함
    const userId = 1; // 임시로 하드코딩
    const notifications = this.notificationsService.findByUserId(userId);
    return { notifications };
  }

  @Post('read-all')
  markAllAsRead(): { success: boolean; updatedCount: number } {
    // TODO: 실제 인증된 사용자 ID를 가져와야 함
    const userId = 1; // 임시로 하드코딩
    const updatedCount = this.notificationsService.markAllAsRead(userId);
    return { success: true, updatedCount };
  }

  @Post(':id/read')
  markAsRead(@Param('id') id: string): { success: boolean } {
    // TODO: 실제 인증된 사용자 ID를 가져와야 함
    const userId = 1; // 임시로 하드코딩
    const notificationId = parseInt(id, 10);
    const success = this.notificationsService.markAsRead(notificationId, userId);
    return { success };
  }

  // 테스트용 엔드포인트: 알림 생성
  @Post('create')
  createNotification(@Body() createNotificationDto: CreateNotificationDto): NotificationDto {
    // 서비스의 create 메소드는 targetUserId를 userId로 받아 처리
    const notification = this.notificationsService.create({
      ...createNotificationDto,
      userId: createNotificationDto.targetUserId,
    });

    // 게이트웨이를 통해 특정 사용자에게 실시간 알림 전송
    if (notification && notification.userId) {
      this.notificationsGateway.sendNotificationToUser(notification.userId, notification);
    }

    return notification;
  }

  @Post('admin/create')
  createAdminNotification(@Body() createNotificationDto: CreateNotificationDto): { success: boolean; message: string } {
    // 관리자 알림은 DB에 저장하지 않고 실시간 브로드캐스트만 수행
    // Spring Boot 서버에서 이미 DB에 저장하므로 중복 저장 방지

    // 브로드캐스트용 알림 객체 생성 (저장하지 않음)
    const broadcastNotification = {
      id: Date.now(), // 임시 ID (저장되지 않음)
      userId: createNotificationDto.targetUserId || 0,
      message: createNotificationDto.message || '',
      category: createNotificationDto.category || 'ADMIN',
      isRead: false,
      link: createNotificationDto.link,
      createdAt: new Date(),
    };

    // 게이트웨이를 통해 관리자 그룹에게 실시간 알림 전송만 수행
    this.notificationsGateway.sendNotificationToAdminGroup(broadcastNotification);

    return {
      success: true,
      message: 'Admin notification broadcasted successfully (not saved to prevent duplication)'
    };
  }
}
