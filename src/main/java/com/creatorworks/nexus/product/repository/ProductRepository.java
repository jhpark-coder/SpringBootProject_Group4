package com.creatorworks.nexus.product.repository;

import com.creatorworks.nexus.product.constant.ProductCategory;
import com.creatorworks.nexus.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory1(ProductCategory category1, Pageable pageable);

    // '모든 상품 조회'는 JpaRepository의 findAll(Pageable pageable)을 사용합니다.
}
