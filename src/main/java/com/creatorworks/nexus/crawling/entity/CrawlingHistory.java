package com.creatorworks.nexus.crawling.entity;

import com.creatorworks.nexus.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawling_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlingHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;  // 크롤링한 카테고리

    @Column(nullable = false)
    private LocalDateTime startTime;  // 시작 시간

    private LocalDateTime endTime;    // 종료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrawlingStatus status;    // 크롤링 상태

    private Integer crawledCount;     // 크롤링된 항목 수

    private String errorMessage;      // 오류 메시지

    @Column(columnDefinition = "TEXT")
    private String details;           // 상세 정보

    public enum CrawlingStatus {
        RUNNING, SUCCESS, FAILED
    }

    @Builder
    public CrawlingHistory(String category) {
        this.category = category;
        this.startTime = LocalDateTime.now();
        this.status = CrawlingStatus.RUNNING;
    }

    public void success(int count, String details) {
        this.status = CrawlingStatus.SUCCESS;
        this.endTime = LocalDateTime.now();
        this.crawledCount = count;
        this.details = details;
    }

    public void fail(String errorMessage) {
        this.status = CrawlingStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
} 