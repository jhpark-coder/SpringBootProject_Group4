package com.creatorworks.nexus.crawling.repository;

import com.creatorworks.nexus.crawling.entity.CrawlingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlingHistoryRepository extends JpaRepository<CrawlingHistory, Long> {
    
    /**
     * 최근 크롤링 히스토리를 조회합니다.
     */
    List<CrawlingHistory> findTop10ByOrderByStartTimeDesc();
    
    /**
     * 특정 카테고리의 크롤링 히스토리를 조회합니다.
     */
    List<CrawlingHistory> findByCategoryOrderByStartTimeDesc(String category);
} 