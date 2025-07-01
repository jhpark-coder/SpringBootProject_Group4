package com.creatorworks.nexus.order.dto;

import lombok.Getter;

@Getter
public class GenderRatioDto {
    private final String gender;
    private final Long count;

    public GenderRatioDto(String gender, Long count) {
        this.gender = gender;
        this.count = count;
    }
}