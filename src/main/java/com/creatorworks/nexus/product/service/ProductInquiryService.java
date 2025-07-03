package com.creatorworks.nexus.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductInquiryService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductInquiryRepository productInquiryRepository;

    @Transactional(readOnly = true)
    public Page<ProductInquiry> findInquiriesByProduct(Long productId, String keyword, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        
        // 키워드가 없으면 DB에서 바로 페이징하여 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return productInquiryRepository.findByProductAndParentIsNull(product, pageable);
        }

        // 키워드가 있으면, 전체 목록을 가져와 애플리케이션 레벨에서 필터링
        List<ProductInquiry> allInquiries = productInquiryRepository.findByProductAndParentIsNull(product, Pageable.unpaged()).getContent();
        
        List<ProductInquiry> filteredInquiries = allInquiries.stream()
                .filter(inquiry -> inquiry.getContent() != null && inquiry.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // 수동으로 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredInquiries.size());
        
        List<ProductInquiry> pageContent = (start > filteredInquiries.size()) ? List.of() : filteredInquiries.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredInquiries.size());
    }

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
                .isSecret(requestDto.getIsSecret() != null && requestDto.getIsSecret())
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
        
        // 권한 검증: 현재 로그인한 사용자가 상품의 판매자인지 확인합니다.
        if (!product.getSeller().getId().equals(writer.getId())) {
            throw new IllegalStateException("답변을 작성할 권한이 없습니다.");
        }

        // ProductInquiry 엔티티(답변)를 생성합니다.
        ProductInquiry reply = ProductInquiry.builder()
                .product(product)
                .writer(writer)
                .content(requestDto.getContent())
                .isSecret(requestDto.getIsSecret() != null && requestDto.getIsSecret())
                .parent(parent) // 부모 문의 설정
                .build();
        
        productInquiryRepository.save(reply);
    }
} 