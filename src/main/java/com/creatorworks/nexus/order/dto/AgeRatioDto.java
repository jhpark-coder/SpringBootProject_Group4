package com.creatorworks.nexus.order.dto;

import lombok.Getter;

@Getter
public class AgeRatioDto {
    private final String ageGroup;
    private final Long count;

    public AgeRatioDto(String ageGroup, Long count) {
        this.ageGroup = ageGroup;
        this.count = count;
    }
}