package com.creatorworks.nexus.auction.Specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.creatorworks.nexus.auction.entity.Auction;

public class AuctionSpecification {

    public static Specification<Auction> byCategory(String primaryCategory, String secondaryCategory) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(primaryCategory) && !"all".equalsIgnoreCase(primaryCategory)) {
                if (StringUtils.hasText(secondaryCategory) && !"all".equalsIgnoreCase(secondaryCategory)) {
                    // 1차, 2차 카테고리 모두 지정된 경우
                    return criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("primaryCategory"), primaryCategory),
                            criteriaBuilder.equal(root.get("secondaryCategory"), secondaryCategory)
                    );
                } else {
                    // 1차 카테고리만 지정된 경우
                    return criteriaBuilder.equal(root.get("primaryCategory"), primaryCategory);
                }
            }
            return null; // 모든 상품 반환
        };
    }
}
