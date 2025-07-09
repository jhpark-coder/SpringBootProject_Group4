import { Injectable } from '@nestjs/common';

export interface NotificationDto {
  id?: number;
  userId: number;
  message: string;
  category: 'SOCIAL' | 'AUCTION' | 'ORDER' | 'ADMIN';
  isRead: boolean;
  link?: string;
  createdAt: Date;
}

@Injectable()
export class NotificationsService {
  private notifications: NotificationDto[] = [];
  private nextId = 1;

  create(createNotificationDto: Partial<NotificationDto>): NotificationDto {
    const notification: NotificationDto = {
      id: this.nextId++,
      userId: createNotificationDto.userId || 0,
      message: createNotificationDto.message || '',
      category: createNotificationDto.category || 'ADMIN',
      isRead: false,
      link: createNotificationDto.link,
      createdAt: new Date(),
    };
    
    this.notifications.push(notification);
    return notification;
  }

  findAll(): NotificationDto[] {
    return this.notifications;
  }

  findOne(id: number): NotificationDto | null {
    return this.notifications.find(notification => notification.id === id) || null;
  }

  findByUserId(userId: number): NotificationDto[] {
    return this.notifications.filter(notification => notification.userId === userId);
  }

  findByCategory(category: string): NotificationDto[] {
    return this.notifications.filter(notification => notification.category === category);
  }

  getUnreadCount(userId: number): number {
    return this.notifications.filter(
      notification => notification.userId === userId && !notification.isRead
    ).length;
  }

  markAsRead(id: number, userId: number): boolean {
    const notification = this.notifications.find(
      n => n.id === id && n.userId === userId
    );
    if (notification) {
      notification.isRead = true;
      return true;
    }
    return false;
  }

  markAllAsRead(userId: number): number {
    let count = 0;
    this.notifications.forEach(notification => {
      if (notification.userId === userId && !notification.isRead) {
        notification.isRead = true;
        count++;
      }
    });
    return count;
  }
}
