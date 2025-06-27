package com.creatorworks.nexus.product.repository;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 특정 사용자가 찜한 상품 목록 조회
    @Query("SELECT w.product FROM Wishlist w WHERE w.member = :member ORDER BY w.wishDate DESC")
    List<Product> findWishedProductsByMember(@Param("member") Member member);

    // 특정 사용자가 특정 상품을 찜했는지 확인
    Optional<Wishlist> findByMemberAndProduct(Member member, Product product);

    // 특정 사용자가 특정 상품을 찜했는지 확인 (boolean 반환)
    boolean existsByMemberAndProduct(Member member, Product product);

    // 특정 사용자의 찜하기 개수 조회
    long countByMember(Member member);

    // 특정 상품의 찜하기 개수 조회
    long countByProduct(Product product);

    // 특정 사용자의 찜하기 목록 조회 (Wishlist 엔티티 반환)
    List<Wishlist> findByMemberOrderByWishDateDesc(Member member);
} 