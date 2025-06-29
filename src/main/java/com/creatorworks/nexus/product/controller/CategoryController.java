package com.creatorworks.nexus.product.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatorworks.nexus.product.dto.CategoryDto;
import com.creatorworks.nexus.product.dto.TagDto;
import com.creatorworks.nexus.product.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/primary")
    public ResponseEntity<List<CategoryDto>> getPrimaryCategories() {
        return ResponseEntity.ok(categoryService.findPrimaryCategories());
    }

    @GetMapping("/{parentId}/secondary")
    public ResponseEntity<List<CategoryDto>> getSecondaryCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.findSecondaryCategories(parentId));
    }

    @GetMapping("/{categoryId}/tags")
    public ResponseEntity<List<TagDto>> getTagsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.findTagsByCategoryId(categoryId));
    }
} 