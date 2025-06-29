package com.creatorworks.nexus.product.service;

import com.creatorworks.nexus.product.dto.CategoryDto;
import com.creatorworks.nexus.product.dto.TagDto;
import com.creatorworks.nexus.product.repository.CategoryRepository;
import com.creatorworks.nexus.product.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

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

    public List<TagDto> findTagsByCategoryId(Long categoryId) {
        return tagRepository.findByCategoryId(categoryId).stream()
                .map(TagDto::new)
                .collect(Collectors.toList());
    }
} 