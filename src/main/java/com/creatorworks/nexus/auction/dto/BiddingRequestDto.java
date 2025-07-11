package com.creatorworks.nexus.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BiddingRequestDto {

    @NotNull(message = "입찰 금액은 필수입니다.")
    @Positive(message = "입찰 금액은 0보다 커야 합니다.")
    private Long amount; // 입찰 금액

    // 입찰자 정보는 Spring Security를 통해 얻으므로 DTO에 포함하지 않습니다.
}