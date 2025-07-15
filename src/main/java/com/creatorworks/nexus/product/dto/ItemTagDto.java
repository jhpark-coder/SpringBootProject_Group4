package com.creatorworks.nexus.product.dto;

import com.creatorworks.nexus.product.entity.ItemTag;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemTagDto {
    
    private Long id;
    private String name;
    
    public ItemTagDto(ItemTag itemTag) {
        this.id = itemTag.getId();
        this.name = itemTag.getName();
    }
} 