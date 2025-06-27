package com.creatorworks.nexus.product.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductDto {
    private String ProductNumber;
    private String ProductTitle;
    private String description;
    private String Price;
    private String Category1;
    private String Category2;
    private String imageUrl;
    private String Heart;
    private String OrderId;
    private String RegId;
    private LocalDateTime regTime;
    private LocalDateTime updateTime;
}
