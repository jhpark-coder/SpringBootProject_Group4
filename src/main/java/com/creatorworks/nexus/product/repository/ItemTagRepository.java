package com.creatorworks.nexus.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creatorworks.nexus.product.entity.ItemTag;

public interface ItemTagRepository extends JpaRepository<ItemTag, Long> {
    Optional<ItemTag> findByName(String name);
} 