package com.creatorworks.nexus.auction.scheduler;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.auction.entity.Bid;
import com.creatorworks.nexus.auction.repository.AuctionRepository;
import com.creatorworks.nexus.auction.repository.BidRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 매분마다 실행하여 종료된 경매 처리
     */
    @Scheduled(cron = "0 * * * * ?") // 매분
    @Transactional
    public void processEndedAuctions() {
        log.info("경매 종료 처리 스케줄러 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 종료된 경매 조회 (auctionEndTime이 현재보다 이전인 경매들)
            List<Auction> endedAuctions = auctionRepository.findByAuctionEndTimeBefore(now);
            
            log.info("종료된 경매 수: {}", endedAuctions.size());
            
            for (Auction auction : endedAuctions) {
                try {
                    processAuctionEnd(auction);
                } catch (Exception e) {
                    log.error("경매 종료 처리 중 오류: auctionId={}, error={}", 
                        auction.getId(), e.getMessage());
                }
            }
            
            log.info("경매 종료 처리 완료");
        } catch (Exception e) {
            log.error("경매 종료 처리 중 전체 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 경매 종료 처리
     */
    private void processAuctionEnd(Auction auction) {
        // 입찰 내역 조회 (가격 내림차순)
        List<Bid> bids = bidRepository.findByAuctionIdOrderByRegTimeDesc(auction.getId());
        
        if (bids.isEmpty()) {
            // 입찰이 없는 경우
            sendNoBidNotification(auction);
        } else {
            // 낙찰자와 실패자들에게 알림
            sendAuctionResultNotifications(auction, bids);
        }
        
        log.info("경매 종료 처리 완료: auctionId={}, totalBids={}", 
            auction.getId(), bids.size());
    }

    /**
     * 입찰이 없는 경우 판매자에게 알림
     */
    private void sendNoBidNotification(Auction auction) {
        try {
            String message = String.format("경매가 종료되었습니다. '%s'에 입찰이 없어 낙찰자가 없습니다.", 
                auction.getName());
            
            Notification notification = Notification.builder()
                    .senderUserId(0L)
                    .targetUserId(auction.getSeller().getId())
                    .message(message)
                    .type("auction_no_bid")
                    .category(NotificationCategory.AUCTION)
                    .isRead(false)
                    .link("/auction/" + auction.getId())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("입찰 없음 알림 전송: auctionId={}, sellerId={}", 
                auction.getId(), auction.getSeller().getId());
        } catch (Exception e) {
            log.error("입찰 없음 알림 전송 실패: auctionId={}, error={}", 
                auction.getId(), e.getMessage());
        }
    }

    /**
     * 경매 결과 알림 전송 (낙찰자 + 실패자)
     */
    private void sendAuctionResultNotifications(Auction auction, List<Bid> bids) {
        if (bids.isEmpty()) return;
        
        Bid winningBid = bids.get(0); // 최고가 입찰
        Member winner = winningBid.getBidder();
        
        // 낙찰자에게 알림
        sendWinnerNotification(auction, winningBid);
        
        // 실패자들에게 알림 (낙찰자 제외)
        Set<Long> loserIds = bids.stream()
                .skip(1) // 최고가 제외
                .map(bid -> bid.getBidder().getId())
                .filter(id -> !id.equals(winner.getId())) // 낙찰자 제외
                .collect(Collectors.toSet());
        
        sendLoserNotifications(auction, loserIds, winningBid.getPrice());
    }

    /**
     * 낙찰자에게 알림
     */
    private void sendWinnerNotification(Auction auction, Bid winningBid) {
        try {
            String message = String.format("축하합니다! '%s' 경매에서 낙찰되었습니다. 낙찰가: %,d원", 
                auction.getName(), winningBid.getPrice());
            
            Notification notification = Notification.builder()
                    .senderUserId(0L)
                    .targetUserId(winningBid.getBidder().getId())
                    .message(message)
                    .type("auction_win")
                    .category(NotificationCategory.AUCTION)
                    .isRead(false)
                    .link("/auction/" + auction.getId())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("낙찰 알림 전송: auctionId={}, winnerId={}, price={}", 
                auction.getId(), winningBid.getBidder().getId(), winningBid.getPrice());
        } catch (Exception e) {
            log.error("낙찰 알림 전송 실패: auctionId={}, error={}", 
                auction.getId(), e.getMessage());
        }
    }

    /**
     * 실패자들에게 알림
     */
    private void sendLoserNotifications(Auction auction, Set<Long> loserIds, Long winningPrice) {
        for (Long loserId : loserIds) {
            try {
                String message = String.format("'%s' 경매에서 낙찰에 실패했습니다. 낙찰가: %,d원", 
                    auction.getName(), winningPrice);
                
                Notification notification = Notification.builder()
                        .senderUserId(0L)
                        .targetUserId(loserId)
                        .message(message)
                        .type("auction_lose")
                        .category(NotificationCategory.AUCTION)
                        .isRead(false)
                        .link("/auction/" + auction.getId())
                        .build();
                
                notificationRepository.save(notification);
                
                log.info("실패 알림 전송: auctionId={}, loserId={}", auction.getId(), loserId);
            } catch (Exception e) {
                log.error("실패 알림 전송 실패: auctionId={}, loserId={}, error={}", 
                    auction.getId(), loserId, e.getMessage());
            }
        }
    }
} 