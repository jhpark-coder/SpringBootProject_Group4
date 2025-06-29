package com.creatorworks.nexus.product.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.creatorworks.nexus.product.entity.Product;

public class ProductSpecification {

    public static Specification<Product> byCategory(String primaryCategory, String secondaryCategory) {
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