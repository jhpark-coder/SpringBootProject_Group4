package com.creatorworks.nexus.product.dto;

import com.creatorworks.nexus.product.entity.Tag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagDto {
    private Long id;
    private String name;

    public TagDto(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }
} 