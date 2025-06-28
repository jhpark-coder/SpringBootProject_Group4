package com.creatorworks.nexus.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.dto.ProductInquiryRequestDto;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.product.repository.ProductInquiryRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductInquiryService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductInquiryRepository productInquiryRepository;

    public void createInquiry(Long productId, ProductInquiryRequestDto requestDto, String userEmail) {
        // 상품과 작성자(회원) 엔티티를 조회합니다.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + productId));
        
        Member writer = memberRepository.findByEmail(userEmail);
        if (writer == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email=" + userEmail);
        }

        // ProductInquiry 엔티티를 생성합니다.
        ProductInquiry inquiry = ProductInquiry.builder()
                .product(product)
                .writer(writer)
                .content(requestDto.getContent())
                .isSecret(requestDto.isSecret())
                .parent(null) // 최상위 문의이므로 부모는 null
                .build();

        // 생성된 문의를 저장합니다.
        productInquiryRepository.save(inquiry);
    }

    @Transactional
    public void createReply(Long productId, Long parentInquiryId, ProductInquiryRequestDto requestDto, String userEmail) {
        // 상품, 작성자, 부모 문의 엔티티를 조회합니다.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. id=" + productId));
        
        Member writer = memberRepository.findByEmail(userEmail);
        if (writer == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email=" + userEmail);
        }

        ProductInquiry parent = productInquiryRepository.findById(parentInquiryId)
                .orElseThrow(() -> new IllegalArgumentException("부모 문의를 찾을 수 없습니다. id=" + parentInquiryId));
        
        // 권한 검증: 현재 로그인한 사용자가 상품의 작가인지 확인합니다.
        if (!product.getAuthor().equals(writer)) {
            throw new IllegalStateException("답변을 작성할 권한이 없습니다.");
        }

        // ProductInquiry 엔티티(답변)를 생성합니다.
        ProductInquiry reply = ProductInquiry.builder()
                .product(product)
                .writer(writer)
                .content(requestDto.getContent())
                .isSecret(requestDto.isSecret())
                .parent(parent) // 부모 문의 설정
                .build();
        
        productInquiryRepository.save(reply);
    }
} 