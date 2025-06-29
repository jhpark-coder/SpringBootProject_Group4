package com.creatorworks.nexus.product.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.product.dto.ProductReviewRequestDto;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductReview;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.repository.ProductReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductReviewRepository productReviewRepository;
    private final OrderRepository orderRepository; // 구매 확인용

    /**
     * 특정 상품의 후기 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<ProductReview> findReviewsByProduct(Long productId, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productReviewRepository.findByProductIdAndCommentContaining(productId, keyword, pageable);
        }
        return productReviewRepository.findAllByProductId(productId, pageable);
    }

    /**
     * 특정 사용자가 특정 상품을 구매했는지 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(Member member, Product product) {
        if (member == null || product == null) {
            return false;
        }
        // OrderRepository를 사용하여 구매 이력 확인
        return orderRepository.existsByBuyerAndProduct(member, product);
    }

    /**
     * 특정 사용자가 특정 상품에 대해 작성한 리뷰를 찾습니다.
     */
    @Transactional(readOnly = true)
    public Optional<ProductReview> findReviewByWriterAndProduct(Member writer, Product product) {
        if (writer == null || product == null) {
            return Optional.empty();
        }
        return productReviewRepository.findByWriterAndProduct(writer, product);
    }

    /**
     * 새로운 후기를 생성합니다.
     */
    public void createReview(Long productId, ProductReviewRequestDto requestDto, Member member) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (!hasUserPurchasedProduct(member, product)) {
            throw new IllegalStateException("상품을 구매한 사용자만 후기를 작성할 수 있습니다.");
        }

        if (productReviewRepository.existsByWriterAndProduct(member, product)) {
            throw new IllegalStateException("이미 이 상품에 대한 리뷰를 작성했습니다.");
        }

        ProductReview review = ProductReview.builder()
                .product(product)
                .writer(member)
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .build();
        productReviewRepository.save(review);
    }

    /**
     * 기존 후기를 수정합니다.
     */
    public void updateReview(Long reviewId, ProductReviewRequestDto reviewDto, Member currentMember) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));

        if (!review.getWriter().equals(currentMember)) {
            throw new IllegalStateException("후기를 수정할 권한이 없습니다.");
        }

        review.update(reviewDto);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double average = productReviewRepository.findAverageRatingByProductId(productId);
        return average == null ? 0.0 : average;
    }
} 