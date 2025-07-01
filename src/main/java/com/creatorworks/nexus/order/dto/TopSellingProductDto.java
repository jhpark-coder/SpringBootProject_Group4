package com.creatorworks.nexus.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopSellingProductDto {

    private Long id;
    private String name;
    private String imageUrl;
    private String authorName; // 작가 이름도 필요할 수 있으니 추가
    private Long salesCount; // 판매량을 담을 필드

    // JPQL의 SELECT NEW 구문에서 사용할 생성자
    public TopSellingProductDto(Long id, String name, String imageUrl, String authorName, Long salesCount) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.authorName = authorName;
        this.salesCount = salesCount;
    }
}
