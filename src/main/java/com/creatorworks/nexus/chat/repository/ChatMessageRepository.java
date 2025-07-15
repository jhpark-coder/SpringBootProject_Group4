package com.creatorworks.nexus.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 특정 사용자와의 채팅 내역 조회 (최신순)
    @Query("SELECT c FROM ChatMessage c WHERE c.sender = :username OR c.recipient = :username ORDER BY c.timestamp ASC")
    List<ChatMessage> findByUsernameOrderByTimestampAsc(@Param("username") String username);
    
    // 사용자명 패턴 매칭으로 채팅 내역 조회 (정규화된 사용자명 지원)
    @Query("SELECT c FROM ChatMessage c WHERE c.sender LIKE %:username% OR c.recipient LIKE %:username% ORDER BY c.timestamp ASC")
    List<ChatMessage> findByUsernamePatternOrderByTimestampAsc(@Param("username") String username);
    
    // 특정 사용자가 보낸 메시지 조회
    List<ChatMessage> findBySenderOrderByTimestampAsc(String sender);
    
    // 특정 사용자가 받은 메시지 조회
    List<ChatMessage> findByRecipientOrderByTimestampAsc(String recipient);
    
    // 관리자가 보낸 메시지 조회
    List<ChatMessage> findBySenderAndRecipientIsNotNullOrderByTimestampAsc(String sender);
    
    // 전체 채팅 내역 조회 (최신순)
    List<ChatMessage> findAllByOrderByTimestampDesc();
    
    // 특정 시간 이후의 메시지 조회
    @Query("SELECT c FROM ChatMessage c WHERE c.timestamp >= :since ORDER BY c.timestamp ASC")
    List<ChatMessage> findMessagesSince(@Param("since") java.time.LocalDateTime since);
    
    // 모든 고유한 채팅 사용자 목록 조회 (관리자 제외, 최근 채팅 순)
    @Query("SELECT DISTINCT c.sender FROM ChatMessage c WHERE c.sender != '관리자' ORDER BY (SELECT MAX(c2.timestamp) FROM ChatMessage c2 WHERE c2.sender = c.sender OR c2.recipient = c.sender) DESC")
    List<String> findAllDistinctUsersOrderByLatestMessage();
    
    // 특정 사용자의 최근 메시지 조회
    @Query("SELECT c FROM ChatMessage c WHERE c.sender = :username OR c.recipient = :username ORDER BY c.timestamp DESC LIMIT 1")
    ChatMessage findLastMessageByUsername(@Param("username") String username);
} 