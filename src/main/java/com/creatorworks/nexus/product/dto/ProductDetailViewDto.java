package com.creatorworks.nexus.product.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductInquiry;

import lombok.Getter;

/**
 * 상품 상세 페이지의 뷰 렌더링에 필요한 모든 데이터를 담는 DTO.
 * 지연 로딩 문제를 회피하고, 뷰에 필요한 데이터만 명시적으로 전달하기 위해 사용됩니다.
 */
@Getter
public class ProductDetailViewDto {

    private final ProductDto product;
    private final List<InquiryDto> inquiries;

    public ProductDetailViewDto(Product product, List<ProductInquiry> inquiries) {
        this.product = new ProductDto(product);
        this.inquiries = inquiries.stream()
                .map(inquiry -> new InquiryDto(inquiry))
                .collect(Collectors.toList());
    }

    /**
     * 상품 정보 DTO
     */
    @Getter
    public static class ProductDto {
        private final Long id;
        private final String name;
        private final int price;
        private final String imageUrl;
        private final String workDescription;
        private final String description;
        private final String primaryCategory;
        private final String secondaryCategory;
        private final String authorName;
        private final String authorEmail;

        public ProductDto(Product product) {
            this.id = product.getId();
            this.name = product.getName();
            this.price = product.getPrice();
            this.imageUrl = product.getImageUrl();
            this.workDescription = product.getWorkDescription();
            this.description = product.getDescription();
            this.primaryCategory = product.getPrimaryCategory();
            this.secondaryCategory = product.getSecondaryCategory();
            this.authorName = product.getAuthor().getName();
            this.authorEmail = product.getAuthor().getEmail();
        }
    }

    /**
     * 문의 정보 DTO
     */
    @Getter
    public static class InquiryDto {
        private final Long id;
        private final String content;
        private final String writerName;
        private final String writerEmail;
        private final boolean isSecret;
        private final LocalDateTime regTime;
        private final boolean isReply;
        private final Long parentId;
        private final List<InquiryDto> children;


        public InquiryDto(ProductInquiry inquiry) {
            this.id = inquiry.getId();
            this.content = inquiry.getContent();
            this.writerName = inquiry.getWriter().getName();
            this.writerEmail = inquiry.getWriter().getEmail();
            this.isSecret = inquiry.isSecret();
            this.regTime = inquiry.getRegTime();
            this.isReply = inquiry.getParent() != null;
            this.parentId = inquiry.getParent() != null ? inquiry.getParent().getId() : null;
            this.children = inquiry.getChildren().stream().map(InquiryDto::new).collect(Collectors.toList());
        }
    }
} 