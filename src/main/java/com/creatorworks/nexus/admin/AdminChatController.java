package com.creatorworks.nexus.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 채팅 대시보드 컨트롤러
 * Socket.IO 기반 채팅 시스템의 관리자 인터페이스 제공
 */
@Controller
@RequestMapping("/admin")
public class AdminChatController {

    /**
     * 관리자 채팅 대시보드 페이지
     * 실시간 채팅 모니터링 및 관리 기능 제공
     */
    @GetMapping("/chat")
    public String chatDashboard() {
        return "admin/chat-dashboard";
    }
} 