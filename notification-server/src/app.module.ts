import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { NotificationsModule } from './notifications/notifications.module';
import { ChatModule } from './chat/chat.module';

@Module({
  imports: [NotificationsModule, ChatModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule { }
