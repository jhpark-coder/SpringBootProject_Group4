package com.creatorworks.nexus.auction.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component // 이 클래스도 Spring이 관리하는 부품(Bean)으로 등록해요.
@RequiredArgsConstructor
@Slf4j
public class BidWebSocketHandler extends TextWebSocketHandler {

    // 접속한 클라이언트(브라우저)들을 관리하기 위한 저장소
    // Key: 경매 ID, Value: 해당 경매 페이지에 접속한 세션들(Set)
    private final Map<Long, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper; // JSON <-> Java 객체 변환기

    // 클라이언트가 웹소켓에 연결되었을 때 호출되는 메소드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket 연결됨: {}", session.getId());
        // 이 단계에서는 아직 어떤 경매방에 들어갈지 모르므로, 연결만 확인합니다.
    }

    // 클라이언트로부터 메시지를 받았을 때 호출되는 메소드
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("메시지 수신: {}", payload);

        // 클라이언트가 보낸 JSON 메시지를 Map 형태로 변환
        Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
        String type = (String) messageMap.get("type");

        // 클라이언트가 처음 들어와서 '경매방 입장' 메시지를 보냈을 때 처리
        if ("ENTER_AUCTION_ROOM".equals(type)) {
            Long auctionId = Long.valueOf(String.valueOf(messageMap.get("auctionId")));
            // 해당 경매 ID의 방에 현재 세션을 추가
            roomSessions.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
            log.info("세션 {} 가 경매방 {} 에 입장했습니다.", session.getId(), auctionId);
        }
    }

    // 클라이언트와 연결이 끊겼을 때 호출되는 메소드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket 연결 끊김: {}, 상태: {}", session.getId(), status);
        // 모든 경매방을 순회하며 현재 세션을 제거
        roomSessions.values().forEach(sessionsInRoom -> sessionsInRoom.remove(session.getId()));
    }

    // [핵심] 특정 경매방에 있는 모든 클라이언트에게 메시지를 방송하는 메소드
    public void broadcastPriceUpdate(Long auctionId, Long newPrice, String highestBidderName) {
        Map<String, WebSocketSession> sessionsInRoom = roomSessions.get(auctionId);

        if (sessionsInRoom == null || sessionsInRoom.isEmpty()) {
            log.info("경매방 {} 에 접속한 사용자가 없어 메시지를 보내지 않습니다.", auctionId);
            return;
        }

        // 보낼 메시지를 JSON 형태로 만듭니다.
        Map<String, Object> messagePayload = Map.of(
                "type", "PRICE_UPDATE",
                "auctionId", auctionId,
                "newPrice", newPrice,
                "highestBidderName", highestBidderName
        );

        try {
            String jsonMessage = objectMapper.writeValueAsString(messagePayload);
            TextMessage textMessage = new TextMessage(jsonMessage);

            // 해당 경매방의 모든 세션에 메시지 전송
            for (WebSocketSession session : sessionsInRoom.values()) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    log.info("경매방 {} 의 세션 {} 에게 가격 업데이트 메시지 전송 완료", auctionId, session.getId());
                }
            }
        } catch (IOException e) {
            log.error("가격 업데이트 메시지 전송 중 오류 발생", e);
        }
    }
} 