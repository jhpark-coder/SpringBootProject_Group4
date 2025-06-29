package com.creatorworks.nexus.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creatorworks.nexus.product.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByCategoryId(Long categoryId);
} 