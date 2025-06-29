package com.creatorworks.nexus.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creatorworks.nexus.product.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();
    List<Category> findByParentId(Long parentId);
    Optional<Category> findByName(String name);
} 