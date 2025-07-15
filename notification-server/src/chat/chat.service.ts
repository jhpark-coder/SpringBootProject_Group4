import { Injectable } from '@nestjs/common';
import { ChatMessageDto } from './dto/chat-message.dto';
import fetch from 'node-fetch';

interface OnlineUser {
    username: string;
    socketId: string;
    joinedAt: Date;
    lastActivity: Date;
}

@Injectable()
export class ChatService {
    private messages: ChatMessageDto[] = [];
    private onlineUsers: Map<string, OnlineUser> = new Map();

    async saveMessage(messageData: ChatMessageDto): Promise<ChatMessageDto> {
        const message: ChatMessageDto = {
            ...messageData,
            timestamp: new Date(),
        };

        // 메모리에 저장 (기존 기능 유지)
        this.messages.push(message);

        // DB에 저장 (Spring Boot 서버로 전송)
        try {
            await this.saveToDatabase(message);
        } catch (error) {
            console.error('DB 저장 실패:', error);
        }

        // 사용자 활동 업데이트
        if (messageData.sender) {
            this.updateUserActivity(messageData.sender);
        }

        return message;
    }

    private async saveToDatabase(message: ChatMessageDto): Promise<void> {
        const apiUrl = process.env.DATABASE_URL || 'http://localhost:8080/api/chat/messages';
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                sender: message.sender,
                content: message.content,
                type: (message.type || '').toUpperCase(),
                recipient: message.recipient
            }),
        });

        if (!response.ok) {
            throw new Error(`DB 저장 실패: ${response.status}`);
        }
    }

    async getChatHistory(userId: string): Promise<ChatMessageDto[]> {
        console.log('🔍 채팅 내역 조회 시작:', userId);
        const baseUrl = (process.env.DATABASE_URL || 'http://localhost:8080').replace('/api/chat/messages', '');

        // 먼저 패턴 매칭으로 시도
        try {
            const patternResponse = await fetch(`${baseUrl}/api/chat/messages/history/pattern/${userId}`);
            console.log('📡 패턴 매칭 DB 조회 응답 상태:', patternResponse.status);
            if (patternResponse.ok) {
                const history = await patternResponse.json();
                console.log('✅ 패턴 매칭으로 조회된 채팅 내역:', history);
                if (history && Array.isArray(history) && history.length > 0) {
                    return history as ChatMessageDto[];
                }
            }
        } catch (error) {
            console.error('❌ 패턴 매칭 DB 조회 중 오류 발생:', error);
        }

        // 패턴 매칭 실패 시 정확한 매칭으로 시도
        try {
            const response = await fetch(`${baseUrl}/api/chat/messages/history/${userId}`);
            console.log('📡 정확한 매칭 DB 조회 응답 상태:', response.status);
            if (response.ok) {
                const history = await response.json();
                console.log('✅ 정확한 매칭으로 조회된 채팅 내역:', history);
                return history as ChatMessageDto[];
            } else {
                console.error('❌ DB 조회 실패 - 상태 코드:', response.status);
                const errorText = await response.text();
                console.error('❌ DB 조회 실패 - 응답 내용:', errorText);
            }
        } catch (error) {
            console.error('❌ DB 조회 중 오류 발생:', error);
        }

        // DB 조회 실패 시 메모리에서 조회 (fallback)
        console.log('🔄 메모리에서 채팅 내역 조회 (fallback)');
        const memoryHistory = this.messages.filter(
            message => message.sender === userId || message.recipient === userId
        );
        console.log('📋 메모리에서 조회된 채팅 내역:', memoryHistory);
        return memoryHistory;
    }

    getAllMessages(): ChatMessageDto[] {
        return [...this.messages];
    }

    clearHistory(userId: string): void {
        this.messages = this.messages.filter(
            message => message.sender !== userId && message.recipient !== userId
        );
    }

    // 온라인 사용자 관리
    addOnlineUser(username: string, socketId: string): void {
        const user: OnlineUser = {
            username,
            socketId,
            joinedAt: new Date(),
            lastActivity: new Date()
        };

        this.onlineUsers.set(username, user);
    }

    removeOnlineUser(username: string): void {
        this.onlineUsers.delete(username);
    }

    removeOnlineUserBySocketId(socketId: string): string | null {
        for (const [username, user] of this.onlineUsers.entries()) {
            if (user.socketId === socketId) {
                this.onlineUsers.delete(username);
                return username;
            }
        }
        return null;
    }

    getOnlineUsers(): string[] {
        return Array.from(this.onlineUsers.keys());
    }

    getOnlineUserCount(): number {
        return this.onlineUsers.size;
    }

    updateUserActivity(username: string): void {
        const user = this.onlineUsers.get(username);
        if (user) {
            user.lastActivity = new Date();
            this.onlineUsers.set(username, user);
        }
    }

    isUserOnline(username: string): boolean {
        return this.onlineUsers.has(username);
    }

    getUserInfo(username: string): OnlineUser | null {
        return this.onlineUsers.get(username) || null;
    }

    // DB에서 모든 채팅 사용자 목록 조회
    async getAllChatUsers(): Promise<string[]> {
        console.log('🔍 DB에서 모든 채팅 사용자 목록 조회 시작');
        const baseUrl = (process.env.DATABASE_URL || 'http://localhost:8080').replace('/api/chat/messages', '');

        try {
            const response = await fetch(`${baseUrl}/api/chat/users`);
            console.log('📡 DB 사용자 목록 조회 응답 상태:', response.status);
            
            if (response.ok) {
                const users = await response.json();
                console.log('✅ DB에서 조회된 사용자 목록:', users);
                return users as string[];
            } else {
                console.error('❌ DB 사용자 목록 조회 실패 - 상태 코드:', response.status);
                const errorText = await response.text();
                console.error('❌ DB 사용자 목록 조회 실패 - 응답 내용:', errorText);
            }
        } catch (error) {
            console.error('❌ DB 사용자 목록 조회 중 오류 발생:', error);
        }

        // DB 조회 실패 시 메모리에서 조회 (fallback)
        console.log('🔄 메모리에서 사용자 목록 조회 (fallback)');
        const memoryUsers = Array.from(new Set(
            this.messages.map(msg => msg.sender).filter(Boolean)
        ));
        console.log('📋 메모리에서 조회된 사용자 목록:', memoryUsers);
        return memoryUsers;
    }

    // 사용자의 최근 메시지 조회
    async getUserLastMessage(userId: string): Promise<any> {
        console.log('🔍 사용자 최근 메시지 조회 시작:', userId);
        const baseUrl = (process.env.DATABASE_URL || 'http://localhost:8080').replace('/api/chat/messages', '');

        try {
            const response = await fetch(`${baseUrl}/api/chat/messages/last/${userId}`);
            console.log('📡 사용자 최근 메시지 조회 응답 상태:', response.status);
            
            if (response.ok) {
                const lastMessage = await response.json();
                console.log('✅ DB에서 조회된 최근 메시지:', lastMessage);
                return lastMessage;
            } else {
                console.error('❌ DB 최근 메시지 조회 실패 - 상태 코드:', response.status);
            }
        } catch (error) {
            console.error('❌ DB 최근 메시지 조회 중 오류 발생:', error);
        }

        // DB 조회 실패 시 메모리에서 조회 (fallback)
        console.log('🔄 메모리에서 최근 메시지 조회 (fallback)');
        const userMessages = this.messages.filter(
            msg => msg.sender === userId || msg.recipient === userId
        );
        
        if (userMessages.length > 0) {
            const lastMessage = userMessages[userMessages.length - 1];
            console.log('📋 메모리에서 조회된 최근 메시지:', lastMessage);
            return lastMessage;
        }
        
        return null;
    }

    // 비활성 사용자 정리 (5분 이상 활동이 없는 사용자)
    cleanupInactiveUsers(): void {
        const now = new Date();
        const inactiveThreshold = 5 * 60 * 1000; // 5분

        for (const [username, user] of this.onlineUsers.entries()) {
            const timeSinceLastActivity = now.getTime() - user.lastActivity.getTime();
            if (timeSinceLastActivity > inactiveThreshold) {
                this.onlineUsers.delete(username);
            }
        }
    }
} 