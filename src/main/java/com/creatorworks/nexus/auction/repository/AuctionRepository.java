package com.creatorworks.nexus.auction.repository;

import com.creatorworks.nexus.auction.entity.Auction;
import com.creatorworks.nexus.member.entity.Member;
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

    /**
     * 특정 판매자가 등록한 모든 경매 목록을 페이징하여 조회합니다.
     * @param seller 판매자(Member) 객체
     * @param pageable 페이지 정보 (정렬 포함)
     * @return 해당 판매자의 경매 Page 객체
     */
    Page<Auction> findBySeller(Member seller, Pageable pageable);
}
