package com.creatorworks.nexus.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    /**
     * 특정 상품에 대한 후기 목록을 페이지별로 조회합니다.
     * @param product 조회할 상품
     * @param pageable 페이지 정보
     * @return 후기 페이지
     */
    Page<ProductReview> findByProduct(Product product, Pageable pageable);

    /**
     * 특정 상품에 대한 후기 목록을 페이지별로 조회합니다.
     * @param product 조회할 상품
     * @param keyword 검색할 키워드
     * @param pageable 페이지 정보
     * @return 후기 페이지
     */
    Page<ProductReview> findByProductAndCommentContaining(Product product, String keyword, Pageable pageable);

    /**
     * 특정 사용자가 특정 상품에 대해 작성한 후기를 찾습니다.
     * @param writer 작성자
     * @param product 상품
     * @return Optional<ProductReview>
     */
    Optional<ProductReview> findByWriterAndProduct(Member writer, Product product);

    boolean existsByWriterAndProduct(Member writer, Product product);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product = :product")
    Optional<Double> findAverageRatingByProduct(@Param("product") Product product);

    Page<ProductReview> findByProductIdAndCommentContaining(Long productId, String keyword, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    Page<ProductReview> findAllByProductId(Long productId, Pageable pageable);
} 