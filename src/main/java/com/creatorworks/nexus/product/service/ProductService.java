package com.creatorworks.nexus.product.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.MemberFollowService;
import com.creatorworks.nexus.product.dto.ProductDto;
import com.creatorworks.nexus.product.dto.ProductPageResponse;
import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.ItemTag;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductHeart;
import com.creatorworks.nexus.product.entity.ProductItemTag;
import com.creatorworks.nexus.product.repository.ItemTagRepository;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import com.creatorworks.nexus.product.repository.ProductItemTagRepository;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.specification.ProductSpecification;
import com.creatorworks.nexus.notification.service.NotificationService;
import com.creatorworks.nexus.notification.dto.FollowNotificationRequest;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * @Service: 이 클래스가 비즈니스 로직을 처리하는 서비스 계층의 컴포넌트임을 Spring에 알립니다.
 * @Transactional: 이 클래스의 모든 public 메소드는 하나의 트랜잭션 단위로 동작합니다.
 *                 메소드 실행 중 예외가 발생하면, 데이터베이스 작업을 모두 롤백하여 데이터 일관성을 유지합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    // 데이터베이스와 상호작용하는 ProductRepository를 주입받습니다.
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ItemTagRepository itemTagRepository;
    private final ProductItemTagRepository productItemTagRepository;
    private final ProductHeartRepository productHeartRepository;
    private final MemberFollowService memberFollowService;
    private final NotificationService notificationService;

    /**
     * 특정 1차 카테고리에 속하는 중복되지 않는 2차 카테고리 목록을 조회합니다.
     * @param primaryCategory 1차 카테고리 이름
     * @return 2차 카테고리 이름의 리스트
     */
    public List<String> findDistinctSecondaryCategories(String primaryCategory) {
        return productRepository.findDistinctSecondaryCategoryByPrimaryCategory(primaryCategory);
    }
    
    /**
     * 카테고리와 페이징 정보를 기반으로 상품 목록을 조회합니다.
     * @param primaryCategory 1차 카테고리
     * @param secondaryCategory 2차 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 상품 응답 DTO
     */
    public ProductPageResponse findAllProducts(String primaryCategory, String secondaryCategory, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.byCategory(primaryCategory, secondaryCategory));
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(ProductDto::new)
                .toList();

        return new ProductPageResponse(
                productDtos,
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getSize(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    /**
     * 카테고리와 페이징 정보를 기반으로 상품 목록을 조회합니다. (팔로우 상태 포함)
     * @param primaryCategory 1차 카테고리
     * @param secondaryCategory 2차 카테고리
     * @param pageable 페이징 정보
     * @param currentUser 현재 로그인한 사용자
     * @return 페이징된 상품 응답 DTO
     */
    public ProductPageResponse findAllProducts(String primaryCategory, String secondaryCategory, Pageable pageable, Member currentUser) {
        Specification<Product> spec = Specification.where(ProductSpecification.byCategory(primaryCategory, secondaryCategory));
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(product -> {
                    boolean isFollowing = false;
                    if (currentUser != null && product.getSeller() != null) {
                        isFollowing = memberFollowService.isFollowing(currentUser.getId(), product.getSeller().getId());
                    }
                    return new ProductDto(product, isFollowing);
                })
                .toList();

        return new ProductPageResponse(
                productDtos,
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getSize(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    /**
     * 페이징 처리된 모든 상품 목록을 조회합니다.
     * @param pageable 페이징 및 정렬 정보.
     * @return 페이징된 상품(Product) 목록.
     */
    public ProductPageResponse findAllProducts(Pageable pageable) {
        // 이 로그는 캐시에 없는 새로운 페이지를 요청할 때만 출력됩니다.
        // DB에서 상품 목록을 조회합니다.
        // ProductRepository를 통해 데이터베이스에서 상품 목록을 조회합니다.
        Page<Product> productPage = productRepository.findAll(pageable);

        // 엔티티 리스트를 DTO 리스트로 변환합니다.
        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(ProductDto::new)
                .toList();

        // 새로운 생성자를 사용하여 응답 DTO를 생성합니다.
        return new ProductPageResponse(
                productDtos,
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getSize(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    /**
     * 상품 ID를 사용하여 특정 상품 하나를 조회합니다.
     * @param id 조회할 상품의 ID.
     * @return 찾아낸 상품(Product) 객체.
     * @throws IllegalArgumentException 해당 ID의 상품이 존재하지 않을 경우 예외를 발생시킵니다.
     */
    public Product findProductById(Long id) {
        // Repository에서 ID로 상품을 찾고, 만약 없다면(.orElseThrow) 예외를 던집니다.
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
    }

    /**
     * 상품 조회와 동시에 조회수를 증가시킵니다. (상품 상세 페이지 조회 시 사용)
     * @param id 조회할 상품의 ID.
     * @return 찾아낸 상품(Product) 객체.
     * @throws IllegalArgumentException 해당 ID의 상품이 존재하지 않을 경우 예외를 발생시킵니다.
     */
    @Transactional
    public Product findProductByIdAndIncrementView(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        product.setViewCount(product.getViewCount() + 1);
        return product;
    }

    /**
     * 새로운 상품을 데이터베이스에 저장합니다.
     * @param request 상품 저장에 필요한 데이터를 담은 DTO(Data Transfer Object).
     * @return 데이터베이스에 저장된 후의 상품(Product) 객체 (생성된 ID 포함).
     */
    @Transactional
    public Product saveProduct(ProductSaveRequest request, String userEmail) {
        Member seller = memberRepository.findByEmail(userEmail);
        if (seller == null) {
            throw new IllegalArgumentException("판매자 정보를 찾을 수 없습니다: " + userEmail);
        }

        // 카테고리 저장 디버그 로그
        // 상품 저장 - 카테고리 및 태그 정보

        Product product = Product.builder()
                .seller(seller)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .workDescription(request.getWorkDescription())
                .tiptapJson(request.getTiptapJson())
                .imageUrl(request.getImageUrl())
                .primaryCategory(request.getPrimaryCategory())
                .secondaryCategory(request.getSecondaryCategory())
                .backgroundColor(request.getBackgroundColor())
                .fontFamily(request.getFontFamily())
                .build();
        
        Product savedProduct = productRepository.save(product);
        saveTags(savedProduct, request.getTags());
        return savedProduct;
    }

    /**
     * 기존 상품의 정보를 수정합니다.
     * @param id 수정할 상품의 ID.
     * @param request 상품 수정에 필요한 데이터를 담은 DTO.
     * @return 정보가 수정된 후의 상품(Product) 객체.
     */
    @Transactional
    public Product updateProduct(Long id, ProductSaveRequest request, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        
        // 상품 수정 권한 체크 (로그인한 사용자가 상품의 seller와 동일한지 확인)
        if (!product.getSeller().getEmail().equals(userEmail)) {
            throw new IllegalStateException("상품을 수정할 권한이 없습니다.");
        }

        // 카테고리 수정 디버그 로그
        // 상품 수정 - 카테고리 및 태그 정보 업데이트

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setTiptapJson(request.getTiptapJson());
        product.setWorkDescription(request.getWorkDescription());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());

        productItemTagRepository.deleteAllByProductId(product.getId());
        saveTags(product, request.getTags());

        return product; // 더티 체킹에 의해 변경 감지 후 업데이트됨
    }

    private void saveTags(Product product, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            ItemTag itemTag = itemTagRepository.findByName(tagName)
                    .orElseGet(() -> itemTagRepository.save(ItemTag.builder().name(tagName).build()));

            ProductItemTag productItemTag = ProductItemTag.builder()
                    .product(product)
                    .itemTag(itemTag)
                    .build();
            
            productItemTagRepository.save(productItemTag);
        }
    }

    /**
     * 상품에 대한 좋아요를 토글(추가/삭제)합니다.
     * @param productId 상품 ID
     * @param userEmail 사용자 이메일
     * @return 좋아요가 추가되면 true, 삭제되면 false를 반환합니다.
     */
    @Transactional
    public boolean toggleHeart(Long productId, String userEmail) {
        Product product = findProductById(productId);
        Member member = memberRepository.findByEmail(userEmail);
        if (member == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail);
        }

        Optional<ProductHeart> existingHeart = productHeartRepository.findByMemberIdAndProductId(member.getId(), productId);

        if (existingHeart.isPresent()) {
            // 좋아요 취소
            productHeartRepository.delete(existingHeart.get());
            return false;
        } else {
            // 좋아요 추가
            ProductHeart heart = new ProductHeart();
            heart.setMember(member);
            heart.setProduct(product);
            productHeartRepository.save(heart);

            // 최초 좋아요에만 알림
            String message = member.getName() + "님이 내 작품 '" + product.getName() + "'에 좋아요를 눌렀습니다";
            String link = "/products/" + productId;
            
            // 좋아요 알림 저장 및 실시간 전송
            var savedNotification = notificationService.saveLikeNotification(
                member.getId(),
                product.getSeller().getId(),
                productId,
                message,
                link
            );
            
            if (savedNotification != null) {
                // 새로운 좋아요 알림인 경우에만 WebSocket 전송
                // 알림 DB 저장 완료
                
                // FollowNotificationRequest를 사용하여 실시간 알림 전송
                FollowNotificationRequest likeNotificationRequest = new FollowNotificationRequest();
                likeNotificationRequest.setTargetUserId(product.getSeller().getId());
                likeNotificationRequest.setSenderUserId(member.getId());
                likeNotificationRequest.setMessage(message);
                likeNotificationRequest.setType("like");
                likeNotificationRequest.setCategory(com.creatorworks.nexus.notification.entity.NotificationCategory.SOCIAL);
                likeNotificationRequest.setLink(link);
                
                notificationService.sendNotification(likeNotificationRequest);
            } else {
                // 중복 좋아요 알림인 경우
                // 알림 중복 방지
            }

            return true;
        }
    }

    /**
     * 특정 사용자가 상품에 좋아요를 눌렀는지 확인합니다.
     * @param productId 상품 ID
     * @param userEmail 사용자 이메일
     * @return 좋아요를 눌렀으면 true, 아니면 false
     */
    public boolean isLikedByUser(Long productId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail);
        if (member == null) {
            return false;
        }
        return productHeartRepository.findByMemberIdAndProductId(member.getId(), productId).isPresent();
    }

    /**
     * 특정 상품의 총 좋아요 개수를 조회합니다.
     * @param productId 상품 ID
     * @return 좋아요 개수
     */
    public long getHeartCount(Long productId) {
        return productHeartRepository.countByProductId(productId);
    }

    /**
     * 여러 상품의 좋아요 개수를 한 번에 조회합니다.
     * @param productIds 상품 ID 리스트
     * @return 상품 ID를 키로, 좋아요 개수를 값으로 갖는 Map
     */
    public Map<Long, Long> getHeartCountsForProducts(List<Long> productIds) {
        Map<Long, Long> heartCounts = new HashMap<>();
        for (Long productId : productIds) {
            heartCounts.put(productId, getHeartCount(productId));
        }
        return heartCounts;
    }

    public List<ProductDto> findTop3PopularProducts(String secondaryCategory) {
        List<Product> products = productRepository.findTop3BySecondaryCategoryOrderByViewCountDesc(secondaryCategory);
        return products.stream()
                       .map(ProductDto::new)
                       .collect(Collectors.toList());
    }

    /**
     * 특정 판매자가 등록한 상품 목록을 페이징하여 조회합니다.
     * @param seller 조회할 판매자 Member 객체
     * @param pageable 페이징 정보
     * @return 페이징된 상품 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<Product> findProductsBySeller(Member seller, Pageable pageable) {
        return productRepository.findBySeller(seller, pageable);
    }

    /**
     * 특정 사용자가 좋아요한 상품 목록을 조회합니다.
     * @param userEmail 사용자 이메일
     * @return 좋아요한 상품 목록
     */
    public List<Product> findLikedProductsByUser(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail);
        if (member == null) {
            return new ArrayList<>();
        }
        
        List<ProductHeart> productHearts = productHeartRepository.findByMember(member);
        return productHearts.stream()
                .map(ProductHeart::getProduct)
                .collect(Collectors.toList());
    }
}
