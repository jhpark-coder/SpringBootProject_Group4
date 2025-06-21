package com.creatorworks.nexus.product.repository;

import com.creatorworks.nexus.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
