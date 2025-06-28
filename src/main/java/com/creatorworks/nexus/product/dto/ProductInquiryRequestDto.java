package com.creatorworks.nexus.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductInquiryRequestDto {

    private String content;

    private boolean isSecret = false; // 체크박스가 선택되지 않으면 false가 기본값으로 전송됩니다.

} 