package com.creatorworks.nexus.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String ADMIN_TOPIC = "/topic/admin";
    private static final String USER_TOPIC_PREFIX = "/topic/messages/";

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // Case 1: An admin is replying to a specific user.
        if (chatMessage.getRecipient() != null && !chatMessage.getRecipient().isEmpty()) {
            // Send the message to the specific user's personal topic.
            messagingTemplate.convertAndSend(USER_TOPIC_PREFIX + chatMessage.getRecipient(), chatMessage);
            // Also send the message to the admin topic so all admins (including the sender) can see the reply.
            messagingTemplate.convertAndSend(ADMIN_TOPIC, chatMessage);
        }
        // Case 2: A user is sending a message.
        else {
            // Send the message to the admin topic for all admins to see.
            messagingTemplate.convertAndSend(ADMIN_TOPIC, chatMessage);
            // Echo the message back to the user's personal topic so they see their own message.
            messagingTemplate.convertAndSend(USER_TOPIC_PREFIX + chatMessage.getSender(), chatMessage);
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", username);

        // Alert ONLY the admins that a new user has connected.
        chatMessage.setContent(username + " 님이 문의를 시작했습니다.");
        messagingTemplate.convertAndSend(ADMIN_TOPIC, chatMessage);
    }
} 