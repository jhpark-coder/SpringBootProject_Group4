package com.creatorworks.nexus.member.service;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.product.entity.ProductReview;
import com.creatorworks.nexus.product.repository.ProductInquiryRepository;
import com.creatorworks.nexus.product.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerBoardService {

    private final ProductReviewRepository reviewRepository;
    private final ProductInquiryRepository inquiryRepository;

    public Page<ProductReview> getReviewsBySeller(Member seller, Pageable pageable) {
        return reviewRepository.findBySeller(seller, pageable);
    }

    public Page<ProductInquiry> getInquiriesBySeller(Member seller, Pageable pageable) {
        return inquiryRepository.findBySeller(seller, pageable);
    }
} 