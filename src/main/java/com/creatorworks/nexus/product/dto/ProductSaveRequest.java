package com.creatorworks.nexus.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSaveRequest {
    private String name;
    private String imageUrl;
    private String primaryCategory;
    private String secondaryCategory;
    private String description;
    private String tiptapJson;
    private String workDescription;
    private String htmlBackup;
    private String backgroundColor;
    private String fontFamily;
    private int price;
} 