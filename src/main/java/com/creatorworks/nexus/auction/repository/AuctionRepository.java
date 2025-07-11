package com.creatorworks.nexus.auction.repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {
    Page<Auction> findAll(Pageable pageable);

    /**
     * ID 목록에 포함된 Product 리스트를 조회합니다.
     * JPA 기본 메서드이지만 순서를 보장하고 명시적으로 사용하기 위해 선언합니다.
     */
    List<Product> findByIdIn(List<Long> ids);
}
