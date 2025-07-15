package com.creatorworks.nexus.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.product.dto.CategoryDto;
import com.creatorworks.nexus.product.dto.ItemTagDto;
import com.creatorworks.nexus.product.entity.Category;
import com.creatorworks.nexus.product.entity.ItemTag;
import com.creatorworks.nexus.product.repository.CategoryRepository;
import com.creatorworks.nexus.product.repository.ItemTagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemTagRepository itemTagRepository;

    public List<CategoryDto> findPrimaryCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    public List<CategoryDto> findSecondaryCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemTagDto> findTagsByCategoryId(Long categoryId) {
        // ItemTag 기반으로 모든 태그를 반환 (카테고리와 무관하게)
        return itemTagRepository.findAll().stream()
                .map(ItemTagDto::new)
                .collect(Collectors.toList());
    }
} 