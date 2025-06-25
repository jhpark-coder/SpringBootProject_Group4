package com.creatorworks.nexus.auction.entity;

import com.creatorworks.nexus.global.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    @Lob
    private String description; // 프로젝트 내용 (tiptapJson, htmlBackup 등)
    @Lob
    private String tiptapJson;
    private String backgroundColor; // 스타일
    private String fontFamily; // 스타일
    private int auctionDuration; // 경매기간 (일)
    private int startBidPrice; // 시작입찰가
    private int buyNowPrice; // 즉시입찰가
}
