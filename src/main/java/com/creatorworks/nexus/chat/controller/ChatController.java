package com.creatorworks.nexus.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatorworks.nexus.chat.entity.ChatMessage;
import com.creatorworks.nexus.chat.entity.ChatMessageType;
import com.creatorworks.nexus.chat.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    /**
     * 채팅 메시지 저장
     */
    @PostMapping
    public ResponseEntity<ChatMessage> saveMessage(@RequestBody ChatMessageRequest request) {
        System.out.println("채팅 메시지 저장 요청: sender=" + request.getSender() + ", content=" + request.getContent() + ", type=" + request.getType() + ", recipient=" + request.getRecipient());
        ChatMessage savedMessage = chatMessageService.saveMessage(
            request.getSender(),
            request.getContent(),
            ChatMessageType.valueOf(request.getType().toUpperCase()),
            request.getRecipient()
        );
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * 특정 사용자와의 채팅 내역 조회
     */
    @GetMapping("/history/{username}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String username) {
        System.out.println("🔍 채팅 내역 조회 요청: username=" + username);
        List<ChatMessage> history = chatMessageService.getChatHistory(username);
        System.out.println("📋 조회된 채팅 내역 수: " + history.size());
        for (ChatMessage msg : history) {
            System.out.println("  - " + msg.getSender() + " -> " + msg.getRecipient() + ": " + msg.getContent());
        }
        return ResponseEntity.ok(history);
    }

    /**
     * 사용자명 패턴 매칭으로 채팅 내역 조회 (정규화된 사용자명 지원)
     */
    @GetMapping("/history/pattern/{username}")
    public ResponseEntity<List<ChatMessage>> getChatHistoryByPattern(@PathVariable String username) {
        System.out.println("🔍 패턴 매칭 채팅 내역 조회 요청: username=" + username);
        List<ChatMessage> history = chatMessageService.getChatHistoryByPattern(username);
        System.out.println("📋 패턴 매칭으로 조회된 채팅 내역 수: " + history.size());
        for (ChatMessage msg : history) {
            System.out.println("  - " + msg.getSender() + " -> " + msg.getRecipient() + ": " + msg.getContent());
        }
        return ResponseEntity.ok(history);
    }

    /**
     * 전체 채팅 내역 조회
     */
    @GetMapping
    public ResponseEntity<List<ChatMessage>> getAllMessages() {
        List<ChatMessage> messages = chatMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    // 요청 DTO
    public static class ChatMessageRequest {
        private String sender;
        private String content;
        private String type;
        private String recipient;

        // Getters and Setters
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
    }
} 