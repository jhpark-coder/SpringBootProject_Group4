package com.creatorworks.nexus.product.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductPageResponse {

    private final List<ProductDto> products;
    private final int currentPage;
    private final int totalPages;
    private final long totalElements;
    private final int pageSize;
    private final boolean isFirst;
    private final boolean isLast;

    public ProductPageResponse(List<ProductDto> products, int currentPage, int totalPages, long totalElements, int pageSize, boolean isFirst, boolean isLast) {
        this.products = products;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }
} 