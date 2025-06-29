package com.creatorworks.nexus.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductSaveRequest {
    private String name;
    private String tiptapJson;
    private String description;
    private Long price;
    private String imageUrl;
    private String primaryCategory;
    private String secondaryCategory;
    private String workDescription;
    private String fontFamily;
    private String backgroundColor;
    private List<String> tags;
}