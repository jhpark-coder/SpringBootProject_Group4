package com.creatorworks.nexus.auction.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 아무나 기본 생성자를 만들지 못하게 막아서 안전성을 높여요.
public class Bid extends BaseEntity { // BaseEntity를 상속받아 생성시간, 수정시간을 자동으로 관리해요.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 입찰 기록의 고유 ID

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 최적화
    @JoinColumn(name = "auction_id") // DB에 auction_id 라는 이름의 컬럼으로 저장돼요.
    private Auction auction; // 어떤 경매에 대한 입찰인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id") // DB에 bidder_id 라는 이름의 컬럼으로 저장돼요.
    private Member bidder; // 누가 입찰했는지 (입찰자)

    @Column(nullable = false)
    private Long price; // 얼마에 입찰했는지

    @Builder
    public Bid(Auction auction, Member bidder, Long price) {
        this.auction = auction;
        this.bidder = bidder;
        this.price = price;
    }
} 