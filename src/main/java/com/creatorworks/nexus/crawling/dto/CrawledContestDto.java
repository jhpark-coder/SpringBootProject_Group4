package com.creatorworks.nexus.crawling.dto;

import java.util.List;

/**
 * 크롤링된 콘테스트 데이터를 담는 DTO
 */
public record CrawledContestDto(
    String author,           // 작가명
    String title,            // 제품 타이틀
    List<String> tags,       // 태그 목록
    String thumbnailPath,    // 썸네일 이미지 경로
    List<String> detailImagePaths  // 상세 이미지 경로 목록
) {} 