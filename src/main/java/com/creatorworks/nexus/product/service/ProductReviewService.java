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
    public Page<ProductReview> findReviewsByProduct(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + productId));
        return productReviewRepository.findByProduct(product, pageable);
    }

    /**
     * 특정 사용자가 특정 상품을 구매했는지 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(Member user, Product product) {
        if (user == null || product == null) {
            return false;
        }
        return orderRepository.existsByBuyerAndProduct(user, product);
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
    public void createReview(Long productId, ProductReviewRequestDto requestDto, String userEmail) {
        Member writer = memberRepository.findByEmail(userEmail);
        if (writer == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email=" + userEmail);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + productId));

        // 사용자가 이 상품을 구매했는지 확인
        if (!hasUserPurchasedProduct(writer, product)) {
            throw new IllegalStateException("상품을 구매한 사용자만 후기를 작성할 수 있습니다.");
        }

        // 이미 작성한 후기가 있는지 확인
        productReviewRepository.findByWriterAndProduct(writer, product).ifPresent(review -> {
            throw new IllegalStateException("이미 후기를 작성했습니다. 수정 기능을 이용해주세요.");
        });

        ProductReview review = ProductReview.builder()
                .product(product)
                .writer(writer)
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .build();

        productReviewRepository.save(review);
    }

    /**
     * 기존 후기를 수정합니다.
     */
    public void updateReview(Long reviewId, ProductReviewRequestDto requestDto, String userEmail) {
        Member writer = memberRepository.findByEmail(userEmail);
        if (writer == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다.");
        }

        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. id=" + reviewId));

        // 후기 작성자 본인인지 확인
        if (!review.getWriter().equals(writer)) {
            throw new IllegalStateException("후기를 수정할 권한이 없습니다.");
        }

        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        // BaseEntity의 @LastModifiedDate가 자동으로 수정 시간을 업데이트합니다.
    }
} 