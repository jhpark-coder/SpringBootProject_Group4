package com.creatorworks.nexus.config;

import com.creatorworks.nexus.auction.handler.BidWebSocketHandler; // 아래에서 만들 핸들러
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration // 이 클래스는 설정 파일임을 Spring에게 알려줘요.
@EnableWebSocket // 웹소켓 기능을 활성화해요.
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    // 아래 2단계에서 만들 웹소켓 핸들러를 주입받습니다.
    private final BidWebSocketHandler bidWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/ws/bid" 라는 경로로 웹소켓 연결 요청이 오면,
        // bidWebSocketHandler가 그 요청을 처리하도록 설정합니다.
        registry.addHandler(bidWebSocketHandler, "/ws/bid")
                .setAllowedOrigins("*"); // 모든 도메인에서의 접속을 허용합니다 (개발 편의를 위해).
    }
}