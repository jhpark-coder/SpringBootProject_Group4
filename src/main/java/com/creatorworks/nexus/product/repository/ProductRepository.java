package com.creatorworks.nexus.product.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;

/**
 * @Repository 어노테이션이 없어도 JpaRepository를 상속하면 Spring이 자동으로 Repository Bean으로 등록해줍니다.
 * 이 인터페이스는 상품(Product) 엔티티에 대한 데이터베이스 작업을 처리합니다.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // JpaRepository<Product, Long> 인터페이스를 상속받는 것만으로도,
    // 기본적인 CRUD(Create, Read, Update, Delete) 메소드들을 자동으로 사용할 수 있습니다.
    //
    // 사용 가능한 주요 메소드 예시:
    // - save(Product): 상품 저장 및 수정
    // - findById(Long id): ID로 상품 조회
    // - findAll(): 모든 상품 조회
    // - findAll(Pageable pageable): 페이징 처리된 상품 목록 조회
    // - deleteById(Long id): ID로 상품 삭제
    // - count(): 전체 상품 개수 조회
    //
    // 만약 복잡한 쿼리가 필요하다면, 이 인터페이스 안에 다음과 같이 메소드를 선언할 수 있습니다.
    // 예: findByName(String productName);
    // Spring Data JPA가 메소드 이름을 분석하여 자동으로 쿼리를 생성해줍니다.

    Page<Product> findAll(Pageable pageable);

    /**
     * 특정 1차 카테고리에 속하는 중복되지 않는 2차 카테고리 목록을 조회합니다.
     * @param primaryCategory 1차 카테고리 이름
     * @return 2차 카테고리 이름의 리스트
     */
    @Query("SELECT DISTINCT p.secondaryCategory FROM Product p WHERE p.primaryCategory = :primaryCategory")
    List<String> findDistinctSecondaryCategoryByPrimaryCategory(@Param("primaryCategory") String primaryCategory);

    List<Product> findTop3BySecondaryCategoryOrderByViewCountDesc(String secondaryCategory);
    
    /**
     * 특정 판매자가 작성한 모든 상품을 조회합니다.
     * @param seller 판매자
     * @return 해당 판매자의 상품 목록
     */
    List<Product> findBySeller(Member seller);

    /**
     * 특정 판매자가 작성한 모든 상품을 페이징하여 조회합니다.
     * @param seller 판매자
     * @param pageable 페이징 정보
     * @return 페이징 처리된 해당 판매자의 상품 목록
     */
    Page<Product> findBySeller(Member seller, Pageable pageable);

    /**
     * 특정 판매자가 작성한 모든 상품을 최신순으로 조회합니다.
     * @param seller 판매자
     * @return 해당 판매자의 상품 목록 (최신순)
     */
    List<Product> findBySellerOrderByRegTimeDesc(Member seller);

    // =================================================================================
    // MainPageService에서 사용하는 메서드들 (추가 및 수정된 부분)
    // =================================================================================

    /**
     * ID 목록에 포함된 Product 리스트를 조회합니다.
     * JPA 기본 메서드이지만 순서를 보장하고 명시적으로 사용하기 위해 선언합니다.
     */
    List<Product> findByIdIn(List<Long> ids);

    /**
     * [삭제 및 대체됨] findRecommendedProducts는 아래의 findRecommendedProductsByInterest로 대체되었습니다.
     * Collection<Object[]> 대신 Collection<List<Object>>를 받고, 정렬 기준이 추가되었습니다.
     */
    // @Query(...)
    // List<Product> findRecommendedProducts(...);

    /**
     * [추가/수정] 로그인 사용자의 관심 카테고리(좋아요, 조회 기반)와 일치하는 상품들을 추천합니다.
     * 특정 상품 목록(구매, 조회, 좋아요)은 추천에서 제외합니다.
     * @param categoryPairs (primaryCategory, secondaryCategory) 형태의 리스트 Set
     * @param excludedProductIds 제외할 상품 ID 목록
     * @param pageable 페이징 정보
     * @return 추천 상품 목록
     */
    /**
     * [추가] 1차 또는 2차 카테고리 목록에 포함되는 상품을 조회합니다.
     * @param primaryCategories 1차 카테고리 목록
     * @param secondaryCategories 2차 카테고리 목록
     * @param excludedProductIds 제외할 상품 ID
     * @param pageable 페이징
     * @return 상품 리스트
     */
    @Query("SELECT p FROM Product p WHERE (p.primaryCategory IN :primaryCategories OR p.secondaryCategory IN :secondaryCategories) AND p.id NOT IN :excludedProductIds ORDER BY p.id DESC")
    List<Product> findByCategoriesAndExcludeIds(
            @Param("primaryCategories") Set<String> primaryCategories,
            @Param("secondaryCategories") Set<String> secondaryCategories,
            @Param("excludedProductIds") List<Long> excludedProductIds,
            Pageable pageable);

    /**
     * [추가] ID 목록에 포함되지 않는 상품들을 페이징하여 조회합니다. (인기 상품으로 채울 때 사용)
     * @param ids 제외할 상품 ID 목록
     * @param pageable 페이징 정보
     * @return 상품 Page
     */
    Page<Product> findByIdNotIn(List<Long> ids, Pageable pageable);
    

}
