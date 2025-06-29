package com.creatorworks.nexus.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.creatorworks.nexus.product.entity.ProductItemTag;

public interface ProductItemTagRepository extends JpaRepository<ProductItemTag, Long> {
    List<ProductItemTag> findAllByProductId(Long productId);

    @Modifying
    @Query("delete from ProductItemTag pit where pit.product.id = :productId")
    void deleteAllByProductId(Long productId);
} 