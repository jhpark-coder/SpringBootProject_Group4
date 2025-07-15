package com.creatorworks.nexus.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.chat.entity.ChatMessage;
import com.creatorworks.nexus.chat.entity.ChatMessageType;
import com.creatorworks.nexus.chat.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    
    /**
     * 채팅 메시지 저장
     */
    public ChatMessage saveMessage(String sender, String content, ChatMessageType type, String recipient) {
        if (sender == null || sender.isEmpty()) throw new IllegalArgumentException("sender is null or empty");
        if (content == null || content.isEmpty()) throw new IllegalArgumentException("content is null or empty");
        if (type == null) throw new IllegalArgumentException("type is null");

        String finalRecipient = recipient;
        // 사용자가 보낸 메시지이고 수신자가 지정되지 않았다면, 수신자를 '관리자'로 설정
        if (!"관리자".equals(sender) && (recipient == null || recipient.isEmpty())) {
            finalRecipient = "관리자";
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .content(content)
                .type(type)
                .recipient(finalRecipient)
                .build();
        
        return chatMessageRepository.save(message);
    }
    
    /**
     * 특정 사용자와의 채팅 내역 조회
     */
    public List<ChatMessage> getChatHistory(String username) {
        return chatMessageRepository.findByUsernameOrderByTimestampAsc(username);
    }
    
    /**
     * 사용자명 패턴 매칭으로 채팅 내역 조회 (정규화된 사용자명 지원)
     */
    public List<ChatMessage> getChatHistoryByPattern(String username) {
        return chatMessageRepository.findByUsernamePatternOrderByTimestampAsc(username);
    }
    
    /**
     * 특정 사용자가 보낸 메시지 조회
     */
    public List<ChatMessage> getMessagesBySender(String sender) {
        return chatMessageRepository.findBySenderOrderByTimestampAsc(sender);
    }
    
    /**
     * 특정 사용자가 받은 메시지 조회
     */
    public List<ChatMessage> getMessagesByRecipient(String recipient) {
        return chatMessageRepository.findByRecipientOrderByTimestampAsc(recipient);
    }
    
    /**
     * 관리자가 보낸 메시지 조회
     */
    public List<ChatMessage> getAdminMessages() {
        return chatMessageRepository.findBySenderAndRecipientIsNotNullOrderByTimestampAsc("관리자");
    }
    
    /**
     * 전체 채팅 내역 조회
     */
    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAllByOrderByTimestampDesc();
    }
    
    /**
     * 특정 시간 이후의 메시지 조회
     */
    public List<ChatMessage> getMessagesSince(java.time.LocalDateTime since) {
        return chatMessageRepository.findMessagesSince(since);
    }
    
    /**
     * 사용자 관련 메시지 삭제 (사용자 탈퇴 시)
     */
    public void deleteUserMessages(String username) {
        List<ChatMessage> userMessages = chatMessageRepository.findByUsernameOrderByTimestampAsc(username);
        chatMessageRepository.deleteAll(userMessages);
    }

    /**
     * 모든 채팅 사용자 목록 조회 (최근 채팅 순)
     */
    public List<String> getAllChatUsers() {
        return chatMessageRepository.findAllDistinctUsersOrderByLatestMessage();
    }

    /**
     * 특정 사용자의 최근 메시지 조회
     */
    public ChatMessage getLastMessage(String username) {
        return chatMessageRepository.findLastMessageByUsername(username);
    }
} 