package com.creatorworks.nexus.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentlyViewedAuctionRedisService {


    private final RedisTemplate<String, String> redisTemplate;

    // Redis ZSet의 Key를 생성하는 헬퍼 메서드
    private String getKey(Long memberId) {
        return "viewHistory:" + memberId;
    }

    /**
     * 사용자가 조회한 상품을 '최근 본 상품' 목록에 추가합니다.
     * ZSet(Sorted Set)을 사용하여 상품 ID를 value로, 조회 시간을 score로 저장합니다.
     *
     * @param memberId  현재 로그인한 사용자의 ID
     * @param auctionId 조회한 상품의 ID
     */
    public void addAuctionToHistory(Long memberId, Long auctionId) {
        String key = getKey(memberId);
        // 현재 시간을 score로 사용하여 최신 항목이 높은 점수를 갖도록 합니다.
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, String.valueOf(auctionId), score);

        // 성능을 위해 목록의 크기를 제한할 수 있습니다 (예: 최신 100개만 유지)
        // redisTemplate.opsForZSet().removeRange(key, 0, -101); // 오래된 항목 삭제
    }

    /**
     * 특정 사용자의 '최근 본 상품' ID 목록을 최신순으로 조회합니다.
     *
     * @param memberId 현재 로그인한 사용자의 ID
     * @param limit    가져올 상품의 최대 개수
     * @return 상품 ID의 리스트 (최신순으로 정렬됨)
     */
    public List<Long> getRecentlyViewedAuctionIds(Long memberId, int limit) {
        String key = getKey(memberId);

        // ZSet에서 score가 높은 순(최신순)으로 상품 ID를 limit 개수만큼 가져옵니다.
        // reverseRange(key, start, end) -> start부터 end까지의 멤버를 가져옴 (0부터 시작)
        Set<String> recentAuctionIdsStr = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);

        if (recentAuctionIdsStr == null || recentAuctionIdsStr.isEmpty()) {
            return Collections.emptyList();
        }

        // Set<String>을 List<Long>으로 변환하여 반환합니다.
        return recentAuctionIdsStr.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

}
