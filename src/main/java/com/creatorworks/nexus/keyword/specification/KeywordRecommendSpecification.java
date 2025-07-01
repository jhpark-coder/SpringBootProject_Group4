package com.creatorworks.nexus.keyword.specification;

import com.creatorworks.nexus.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class KeywordRecommendSpecification {
    /**
     * 여러 키워드가 Product의 다양한 필드(제목, 설명, 내용, 카테고리, 작가명 등)에 포함되는지 OR로 검색하는 Specification
     * (실제 점수 계산은 서비스에서 처리)
     */
    public static Specification<Product> containsKeywordsInFields(List<String> keywords) {
        return (root, query, cb) -> {
            if (keywords == null || keywords.isEmpty()) return null;
            // title, description, workDescription, tiptapJson, primaryCategory, secondaryCategory, author.name
            Predicate[] predicates = keywords.stream().flatMap(keyword -> {
                String likePattern = "%" + keyword + "%";
                return java.util.stream.Stream.of(
                    cb.like(root.get("name"), likePattern),
                    cb.like(root.get("description"), likePattern),
                    cb.like(root.get("workDescription"), likePattern),
                    cb.like(root.get("tiptapJson"), likePattern),
                    cb.like(root.get("primaryCategory"), likePattern),
                    cb.like(root.get("secondaryCategory"), likePattern),
                    cb.like(root.get("author").get("name"), likePattern)
                );
            }).toArray(Predicate[]::new);
            return cb.or(predicates);
        };
    }
} 