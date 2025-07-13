package com.creatorworks.nexus.auction.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // JSON 데이터를 객체로 변환하려면 Setter나 생성자가 필요해요.
@NoArgsConstructor
public class BidRequestDto {

    private Long price; // 사용자가 입력한 입찰 금액

    // 나중에 테스트 코드 등을 만들 때 편리하도록 생성자를 추가해 둘 수 있어요.
    public BidRequestDto(Long price) {
        this.price = price;
    }
} 