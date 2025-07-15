package com.creatorworks.nexus.auction.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name="auction")
@Entity
public class Auction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 프로젝트명
    @Lob
    private String imageUrl; // 대표이미지 (Base64 이미지 데이터 지원)
    private String primaryCategory; // 1차 카테고리
    private String secondaryCategory; // 2차 카테고리
    @Column(columnDefinition = "TEXT")
    private String description; // 프로젝트 내용 (tiptapJson, htmlBackup 등)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String tiptapJson;
    @Column(columnDefinition = "TEXT")
    private String workDescription; // 작품 설명
    private String backgroundColor; // 스타일
    private String fontFamily; // 스타일
    private int auctionDuration; // 경매기간 (일)
    private Long startBidPrice;
    private Long buyNowPrice;
    private Long currentPrice; // <<< 새로 추가된 부분 (1/2) : 현재 최고 입찰가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bidder_id")
    private Member highestBidder; // <<< 새로 추가된 부분 (2/2) : 현재 최고 입찰자
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;

    private LocalDateTime auctionEndTime;
    
    // 남은 시간을 계산하는 메서드 (템플릿에서 사용)
    public String getRemainingTime() {
        if (auctionEndTime == null) {
            return "00:00:00";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auctionEndTime)) {
            return "00:00:00";
        }
        
        java.time.Duration duration = java.time.Duration.between(now, auctionEndTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionItemTag> itemTags = new ArrayList<>();

    @Builder
    public Auction(Member seller, String name, Long startBidPrice, Long buyNowPrice, String description,
                   String workDescription, String tiptapJson, String imageUrl, String primaryCategory,
                   String secondaryCategory, LocalDateTime auctionEndTime, String backgroundColor, String fontFamily) {
        this.seller = seller;
        this.name = name;
        this.startBidPrice = startBidPrice;
        this.buyNowPrice = buyNowPrice;
        this.currentPrice = startBidPrice;
        this.description = description;
        this.workDescription = workDescription;
        this.tiptapJson = tiptapJson;
        this.imageUrl = imageUrl;
        this.primaryCategory = primaryCategory;
        this.secondaryCategory = secondaryCategory;
        this.auctionEndTime = auctionEndTime;
        this.backgroundColor = backgroundColor;
        this.fontFamily = fontFamily;
        this.viewCount = 0L;
    }
}
