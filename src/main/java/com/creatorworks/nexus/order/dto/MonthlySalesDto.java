package com.creatorworks.nexus.order.dto;

import lombok.Getter;

@Getter
public class MonthlySalesDto {
    private final Integer year;
    private final Integer month;
    private final Long salesCount; // 총 판매액

    public MonthlySalesDto(Integer year, Integer month, Long salesCount) {
        this.year = year;
        this.month = month;
        this.salesCount = salesCount;
    }
}