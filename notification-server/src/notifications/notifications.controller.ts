import { Controller, Post, Body } from '@nestjs/common';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { NotificationsGateway } from './notifications.gateway';

@Controller('api/notifications')
export class NotificationsController {
  constructor(
    private readonly notificationsGateway: NotificationsGateway,
  ) { }

  // Java에서 보낸 알림을 받아서 웹소켓으로 전송
  @Post('create')
  createNotification(@Body() createNotificationDto: CreateNotificationDto): { success: boolean; message: string } {
    // Java에서 이미 DB에 저장된 알림을 받아서 웹소켓으로만 전송
    if (createNotificationDto.targetUserId) {
      this.notificationsGateway.sendNotificationToUser(createNotificationDto.targetUserId, createNotificationDto);
    }

    return {
      success: true,
      message: 'Notification sent successfully'
    };
  }

  // 관리자 알림 브로드캐스트
  @Post('admin/create')
  createAdminNotification(@Body() createNotificationDto: CreateNotificationDto): { success: boolean; message: string } {
    // 관리자 그룹에게 실시간 알림 전송
    this.notificationsGateway.sendNotificationToAdminGroup(createNotificationDto);

    return {
      success: true,
      message: 'Admin notification broadcasted successfully'
    };
  }
}
