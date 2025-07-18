package com.creatorworks.nexus.product.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.config.CategoryConfig;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.service.MemberFollowService;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.repository.OrderRepository;
import com.creatorworks.nexus.order.service.PointService;
import com.creatorworks.nexus.product.dto.ProductDto;
import com.creatorworks.nexus.product.dto.ProductInquiryRequestDto;
import com.creatorworks.nexus.product.dto.ProductPageResponse;
import com.creatorworks.nexus.product.dto.ProductReviewRequestDto;
import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.product.entity.ProductReview;
import com.creatorworks.nexus.product.service.ProductInquiryService;
import com.creatorworks.nexus.product.service.ProductReviewService;
import com.creatorworks.nexus.product.service.ProductService;
import com.creatorworks.nexus.product.service.RecentlyViewedProductRedisService;
import com.creatorworks.nexus.util.CategoryConverter;
import com.creatorworks.nexus.util.tiptap.TipTapDocument;
import com.creatorworks.nexus.util.tiptap.TipTapNode;
import com.creatorworks.nexus.util.tiptap.TipTapRenderer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Controller: 이 클래스가 Spring MVC의 컨트롤러임을 나타냅니다.
 *              주로 View(HTML 페이지)를 반환하거나 API 엔드포인트 역할을 합니다.
 * @RequiredArgsConstructor: final 필드에 대한 생성자를 자동으로 생성하여,
 *                           의존성 주입(Dependency Injection)을 간편하게 처리합니다.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    // final 키워드와 @RequiredArgsConstructor에 의해, Spring이 자동으로 ProductService의 인스턴스를 주입해줍니다. (생성자 주입)
    private final ProductService productService;
    private final ProductInquiryService productInquiryService;
    private final ProductReviewService productReviewService;
    private final MemberRepository memberRepository;
    private final CategoryConfig categoryConfig;
    private final TipTapRenderer tipTapRenderer;
    private final ObjectMapper objectMapper;
    private final MemberFollowService memberFollowService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RecentlyViewedProductRedisService recentlyViewedProductRedisService;
    private final OrderRepository orderRepository;
    @Qualifier("orderPointService")
    private final PointService pointService;




    /**
     * 상품 목록 데이터를 JSON 형태로 반환하는 API 엔드포인트입니다. (무한 스크롤 기능에서 사용)
     * @GetMapping: HTTP GET 요청을 이 메소드에 매핑합니다.
     * @ResponseBody: 이 어노테이션은 메소드의 반환값이 뷰 이름이 아니라,
     *                HTTP 응답 본문(Response Body)에 직접 작성되어야 함을 나타냅니다.
     *                객체를 반환하면 Spring이 자동으로 JSON으로 변환해줍니다.
     * @param pageable URL 파라미터(예: ?page=0&size=20)를 통해 전달된 페이징 정보를 담는 객체입니다.
     * @return 페이징 처리된 상품(Product) 목록.
     */
    @GetMapping("/api/products")
    @ResponseBody
    public ProductPageResponse getProducts(Pageable pageable) {
        // ProductService를 통해 페이징 및 DTO 변환이 완료된 결과를 받아 그대로 반환합니다.
        return productService.findAllProducts(pageable);
    }

    /**
     * 특정 상품의 상세 정보 페이지("/products/{id}")를 렌더링합니다.
     * @return 렌더링할 뷰의 이름 ("product/productDetail")
     */
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id,
                                @Qualifier("inquiryPageable") @PageableDefault(size = 4, sort = "regTime", direction = Sort.Direction.DESC) Pageable inquiryPageable,
                                @Qualifier("reviewPageable") @PageableDefault(size = 10, sort = "regTime", direction = Sort.Direction.DESC) Pageable reviewPageable,
                                @RequestParam(value = "reviewKeyword", required = false) String reviewKeyword,
                                @RequestParam(value = "inquiryKeyword", required = false) String inquiryKeyword,
                                Principal principal,
                                Model model) {
        Product product = productService.findProductByIdAndIncrementView(id);
        
        // 문의 관련 (검색 기능 추가)
        Page<ProductInquiry> inquiryPage = productInquiryService.findInquiriesByProduct(id, inquiryKeyword, inquiryPageable);

        // 후기 관련
        Page<ProductReview> reviewPage = productReviewService.findReviewsByProduct(id, reviewKeyword, reviewPageable);
        double averageRating = productReviewService.getAverageRating(id);

        // --- 좋아요 관련 로직 추가 ---
        long heartCount = productService.getHeartCount(id);
        boolean isLiked = false;
        // ---

        // 현재 로그인한 사용자 정보 및 후기 작성 상태 조회
        Member currentMember = null;
        boolean canWriteReview = false;
        boolean canViewContent = false; // 컨텐츠 열람 가능 여부
        boolean isFollowing = false; // 팔로우 상태
        Optional<ProductReview> existingReview = Optional.empty();

        if (principal != null) {
            currentMember = memberRepository.findByEmail(principal.getName());
            if(currentMember != null) {
                // --- 좋아요 상태 확인 로직 추가 ---
                isLiked = productService.isLikedByUser(id, principal.getName());
                // ---

                // --- 팔로우 상태 확인 로직 추가 ---
                if (product.getSeller() != null) {
                    isFollowing = memberFollowService.isFollowing(currentMember.getId(), product.getSeller().getId());
                }
                // ---

                // ==============================================================
                //      ★★★  20250701 최근 본 상품을 위한 로직 추가 ★★★
                // ==============================================================
                // --- ★★★ Redis 기록 로직 전체를 아래 코드로 교체 ★★★ ---
                // 1. 개인별 최근 본 상품 기록 (서비스 사용)
                recentlyViewedProductRedisService.addProductToHistory(currentMember.getId(), id);
                log.info("사용자 ID {}의 최근 본 상품 기록 추가 (서비스 호출): 상품 ID {}", currentMember.getId(), id);

                // 2. 카테고리별 조회수 통계 기록 (새로운 로직)
                if (product.getPrimaryCategory() != null && product.getSecondaryCategory() != null) {
                    // 오늘 날짜 키 (예: "categoryViewCount:2025-07-01")
                    String dailyCountKey = "categoryViewCount:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    // "primary:secondary" 형태의 멤버 (예: "artwork:포토그래피")
                    String categoryMember = product.getPrimaryCategory() + ":" + product.getSecondaryCategory();

                    // 해당 카테고리 멤버의 점수를 1 증가시킴
                    redisTemplate.opsForZSet().incrementScore(dailyCountKey, categoryMember, 1);
                    // 7일이 지난 데이터는 자동으로 사라지도록 TTL(Time To Live) 설정
                    redisTemplate.expire(dailyCountKey, 8, TimeUnit.DAYS);

                    log.info("카테고리 조회수 증가: Key='{}', Member='{}'", dailyCountKey, categoryMember);
                }
                // =======================================================

                // 1. 실제 구매 여부를 확인하여 후기 작성 권한 설정
                canWriteReview = productReviewService.hasUserPurchasedProduct(currentMember, product);

                // 2. 판매자 본인인지 확인 (ID를 직접 비교하여 안정성 확보)
                boolean isSeller = product.getSeller() != null && currentMember.getId().equals(product.getSeller().getId());

                // 3. 컨텐츠 열람 권한 설정 (구매자 또는 관리자 또는 판매자)
                canViewContent = canWriteReview || isSeller || currentMember.getRole() == com.creatorworks.nexus.member.constant.Role.ADMIN;

                // 4. 이미 작성한 후기가 있는지 확인
                if (canWriteReview) {
                    existingReview = productReviewService.findReviewByWriterAndProduct(currentMember, product);
                }
            }
        }
        
        // Tiptap JSON 컨텐츠 처리
        String contentHtml = "";
        boolean hasPaywall = false; // 페이월 존재 여부 확인
        String tiptapJson = product.getTiptapJson();
        if (tiptapJson != null && !tiptapJson.isEmpty()) {
            try {
                log.info("Processing tiptapJson for product ID {}: {}", id, tiptapJson);
                TipTapDocument document = objectMapper.readValue(tiptapJson, TipTapDocument.class);
                List<TipTapNode> nodesToRender = document.getContent();

                // 페이월 노드가 있는지 확인
                for (TipTapNode node : nodesToRender) {
                    if ("paywall".equals(node.getType())) {
                        hasPaywall = true;
                        break;
                    }
                }

                if (!canViewContent && hasPaywall) { // 페이월이 있고 구매하지 않은 경우에만 콘텐츠 제한
                    int paywallIndex = -1;
                    for (int i = 0; i < nodesToRender.size(); i++) {
                        if ("paywall".equals(nodesToRender.get(i).getType())) {
                            paywallIndex = i;
                            break;
                        }
                    }
                    if (paywallIndex != -1) {
                        // subList가 반환하는 view 대신 새로운 리스트를 생성하여 안정성을 높입니다.
                        nodesToRender = new ArrayList<>(nodesToRender.subList(0, paywallIndex));
                    }
                }

                contentHtml = tipTapRenderer.render(nodesToRender);

            } catch (JsonProcessingException e) {
                // JSON 파싱 실패 시 로깅 또는 예외 처리
                // 여기서는 간단히 빈 문자열로 대체
                contentHtml = "<p>콘텐츠를 불러오는 데 실패했습니다.</p>";
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("contentHtml", contentHtml); // 렌더링된 HTML 추가
        model.addAttribute("inquiryPage", inquiryPage);
        model.addAttribute("inquiryKeyword", inquiryKeyword);
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewKeyword", reviewKeyword);
        model.addAttribute("currentMember", currentMember);
        model.addAttribute("canWriteReview", canWriteReview);
        model.addAttribute("canViewContent", canViewContent);
        model.addAttribute("hasPaywall", hasPaywall); // 페이월 존재 여부 추가
        model.addAttribute("existingReview", existingReview.orElse(null));
        // --- 좋아요 관련 모델 속성 추가 ---
        model.addAttribute("heartCount", heartCount);
        model.addAttribute("isLiked", isLiked);
        // ---
        // --- 팔로우 관련 모델 속성 추가 ---
        model.addAttribute("isFollowing", isFollowing);
        // ---
        
        // --- 태그 정보 추가 ---
        List<String> allTagNames = product.getItemTags().stream()
                .map(productItemTag -> productItemTag.getItemTag().getName())
                .toList();
        
        // 카테고리를 한글로 변환해서 모델에 추가
        String koreanPrimaryCategory = CategoryConverter.convertPrimaryCategoryToKorean(product.getPrimaryCategory());
        String koreanSecondaryCategory = CategoryConverter.convertSecondaryCategoryToKorean(product.getSecondaryCategory());
        
        model.addAttribute("koreanPrimaryCategory", koreanPrimaryCategory);
        model.addAttribute("koreanSecondaryCategory", koreanSecondaryCategory);
        
        // 카테고리를 제외한 순수 태그만 필터링 (영어 + 한글 모두 제외)
        List<String> pureTagNames = allTagNames.stream()
                .filter(tagName -> !tagName.equals(product.getPrimaryCategory()))      // 영어 1차 카테고리 제외
                .filter(tagName -> !tagName.equals(product.getSecondaryCategory()))    // 영어 2차 카테고리 제외
                .filter(tagName -> !tagName.equals(koreanPrimaryCategory))             // 한글 1차 카테고리 제외
                .filter(tagName -> !tagName.equals(koreanSecondaryCategory))           // 한글 2차 카테고리 제외
                .toList();
        
        model.addAttribute("tagNames", pureTagNames);
        
        // 카테고리 및 태그 디버그 로그
        log.debug("🔍 상품 {} 정보:", id);
        log.debug("  - primaryCategory: '{}'", product.getPrimaryCategory());
        log.debug("  - secondaryCategory: '{}'", product.getSecondaryCategory());
        log.debug("  - koreanPrimaryCategory: '{}'", koreanPrimaryCategory);
        log.debug("  - koreanSecondaryCategory: '{}'", koreanSecondaryCategory);
        log.debug("  - 전체 태그 목록: {}", allTagNames);
        log.debug("  - 순수 태그 목록: {}", pureTagNames);
        // ---

        return "product/productDetail";
    }

    /**
     * 특정 상품 한 개의 데이터를 JSON 형태로 반환하는 API 엔드포인트입니다.
     * @param id 상품 ID
     * @return 상품 데이터(JSON) 또는 404 Not Found
     */
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id) {
        Product product = productService.findProductById(id);
        return ResponseEntity.ok(new ProductDto(product));
    }

    @GetMapping("/api/products/popular")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> getPopularProducts(@RequestParam("secondaryCategory") String secondaryCategory) {
        List<ProductDto> popularProducts = productService.findTop3PopularProducts(secondaryCategory);
        return ResponseEntity.ok(popularProducts);
    }

    /**
     * 새로운 상품을 저장하는 API 엔드포인트입니다. (웹 에디터에서 사용)
     * @PostMapping: HTTP POST 요청을 처리합니다.
     * @RequestBody: 요청 본문의 JSON 데이터를 ProductSaveRequest 객체로 자동 변환합니다.
     * @param request 상품 저장에 필요한 데이터 (상품명, 에디터 내용 등).
     * @return 저장된 상품의 고유 ID.
     */
    @PostMapping("/api/products")
    @ResponseBody
    public Long saveProduct(@RequestBody ProductSaveRequest request, Principal principal) {
        // 개발 환경에서는 principal이 null일 수 있으므로, 임시 사용자 정보를 사용합니다.
        String userEmail = (principal != null) ? principal.getName() : "seller@test.com"; // 임시 이메일
        Product saved = productService.saveProduct(request, userEmail);
        return saved.getId();
    }

    /**
     * 기존 상품의 정보를 수정하는 API 엔드포인트입니다. (웹 에디터에서 사용)
     * @PutMapping("/{id}"): HTTP PUT 요청을 처리하며, 주로 리소스 전체를 수정할 때 사용됩니다.
     * @param id 수정할 상품의 ID.
     * @param request 상품 수정에 필요한 데이터.
     * @return 수정된 상품의 고유 ID.
     */
    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductSaveRequest request, Principal principal) {
        // 개발 환경에서는 principal이 null일 수 있으므로, 임시 사용자 정보를 사용합니다.
        String userEmail = (principal != null) ? principal.getName() : "author@test.com"; // 임시 이메일
        try {
            Product updated = productService.updateProduct(id, request, userEmail);
            return ResponseEntity.ok(updated.getId());
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 서비스 계층에서 발생한 예외(권한 없음, 잘못된 ID 등) 처리
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 상품 저장 또는 수정 후, 해당 상품의 상세 페이지로 리다이렉트합니다.
     * @param id 결과를 확인할 상품의 ID.
     * @return 상품 상세 페이지로의 리다이렉트 경로.
     */
    @GetMapping("/products/result/{id}")
    public String productResultRedirect(@PathVariable Long id) {
        // 상세 페이지 URL로 리다이렉트
        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable("productId") Long productId,
                               @RequestBody ProductReviewRequestDto reviewDto,
                               Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Member currentMember = memberRepository.findByEmail(principal.getName());
            if (currentMember == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }
            productReviewService.createReview(productId, reviewDto, currentMember);
            return ResponseEntity.ok("후기가 성공적으로 등록되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/products/{id}/inquiries")
    public String createInquiry(@PathVariable("id") Long productId,
                                ProductInquiryRequestDto inquiryDto,
                                Principal principal) {
        
        String userEmail = principal.getName();
        productInquiryService.createInquiry(productId, inquiryDto, userEmail);
        
        return "redirect:/products/" + productId;
    }

    @PostMapping("/products/{productId}/inquiries/{inquiryId}/replies")
    public String createReply(@PathVariable("productId") Long productId,
                              @PathVariable("inquiryId") Long inquiryId,
                              ProductInquiryRequestDto inquiryDto,
                              Principal principal) {
        
        String userEmail = principal.getName();
        productInquiryService.createReply(productId, inquiryId, inquiryDto, userEmail);

        return "redirect:/products/" + productId;
    }

    // 후기 수정
    @PutMapping("/products/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable("reviewId") Long reviewId,
                                          @RequestBody ProductReviewRequestDto reviewDto,
                                          Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Member currentMember = memberRepository.findByEmail(principal.getName());
            if (currentMember == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }
            productReviewService.updateReview(reviewId, reviewDto, currentMember);
            return ResponseEntity.ok().body("후기가 성공적으로 수정되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 카테고리별 상품 그리드 뷰 페이지를 렌더링합니다.
     * @param categoryName 1차 카테고리 이름 (예: "artwork", "java")
     * @param page URL로 전달되는 페이지 번호 (1부터 시작)
     * @param secondaryCategory 2차 카테고리 이름 (필수 아님)
     * @param model 뷰에 데이터를 전달하기 위한 모델 객체
     * @param principal 현재 로그인한 사용자 정보
     * @return 렌더링할 뷰의 이름 ("product/category_grid")
     */
    @GetMapping("/products/category/{categoryName}")
    public String categoryGridView(@PathVariable String categoryName,
                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "secondary", defaultValue = "all") String secondaryCategory,
                                   Model model,
                                   Principal principal) {

        // 1. 초기 상품 데이터 로드 (페이지의 첫 16개)
        // URL의 page 파라미터(uiPage)는 1부터 시작, 100개 단위. API 페이지는 16개 단위.
        long itemsPerApiPage = 16;
        long apiPagesPerUiPage = 6; // 16 * 6 = 96개, 약 100개
        long initialApiPage = (page - 1) * apiPagesPerUiPage;

        Pageable initialPageable = PageRequest.of((int)initialApiPage, (int)itemsPerApiPage, Sort.by(Sort.Direction.DESC, "regTime"));
        
        // 현재 로그인한 사용자 정보 가져오기
        Member currentUser = null;
        if (principal != null) {
            currentUser = memberRepository.findByEmail(principal.getName());
        }
        
        ProductPageResponse initialProductPage = productService.findAllProducts(categoryName, secondaryCategory, initialPageable, currentUser);

        // 2. 전체 2차 카테고리 목록 조회 (버튼용)
        List<String> secondaryCategories = categoryConfig.getSecondaryCategories(categoryName);
        
        // 3. 페이지네이션 계산 (전체 아이템 수 / 페이지당 아이템 100개)
        long totalItems = initialProductPage.getTotalElements();
        long itemsPerUiPage = itemsPerApiPage * apiPagesPerUiPage;
        long totalPages = (totalItems == 0) ? 1 : (long) Math.ceil((double) totalItems / itemsPerUiPage);

        // 4. 뷰에 데이터 전달
        model.addAttribute("primaryCategory", categoryName);
        model.addAttribute("secondaryCategory", secondaryCategory); // 현재 선택된 2차 카테고리
        model.addAttribute("secondaryCategories", secondaryCategories);
        model.addAttribute("initialProductPage", initialProductPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "product/category_grid";
    }

    /**
     * 카테고리별 상품 목록 데이터를 JSON 형태로 반환하는 API 엔드포인트입니다.
     * @param primaryCategory 1차 카테고리
     * @param secondaryCategory 2차 카테고리 (필수 아님)
     * @param pageable 페이징 정보
     * @param principal 현재 로그인한 사용자 정보
     * @return 페이징 처리된 상품 데이터
     */
    @GetMapping("/api/products/category")
    @ResponseBody
    public ProductPageResponse getProductsByCategory(
            @RequestParam("primary") String primaryCategory,
            @RequestParam(value = "secondary", required = false, defaultValue = "all") String secondaryCategory,
            Pageable pageable,
            Principal principal) {
        
        // 현재 로그인한 사용자 정보 가져오기
        Member currentUser = null;
        if (principal != null) {
            currentUser = memberRepository.findByEmail(principal.getName());
        }
        
        return productService.findAllProducts(primaryCategory, secondaryCategory, pageable, currentUser);
    }

    // --- 좋아요 관련 API 추가 ---

    /**
     * 상품에 대한 '좋아요'를 토글(추가/삭제)합니다.
     * @param id 상품 ID
     * @param principal 현재 로그인한 사용자 정보
     * @return 좋아요 상태와 개수를 담은 응답
     */
    @PostMapping("/api/products/{id}/heart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleHeart(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        String userEmail = principal.getName();
        boolean isLiked = productService.toggleHeart(id, userEmail);
        long heartCount = productService.getHeartCount(id);
        
        Map<String, Object> response = Map.of("isLiked", isLiked, "heartCount", heartCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 상품의 '좋아요' 상태와 개수를 조회합니다.
     * @param id 상품 ID
     * @param principal 현재 로그인한 사용자 정보
     * @return 좋아요 상태와 개수를 담은 응답
     */
    @GetMapping("/api/products/{id}/heart/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHeartStatus(@PathVariable Long id, Principal principal) {
        long heartCount = productService.getHeartCount(id);
        boolean isLiked = false;
        
        if (principal != null) {
            isLiked = productService.isLikedByUser(id, principal.getName());
        }
        
        Map<String, Object> response = Map.of("isLiked", isLiked, "heartCount", heartCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 상품의 '좋아요' 개수만 조회합니다.
     * @param id 상품 ID
     * @return 좋아요 개수를 담은 응답
     */
    @GetMapping("/api/products/{id}/heart/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHeartCount(@PathVariable Long id) {
        long heartCount = productService.getHeartCount(id);
        return ResponseEntity.ok(Map.of("heartCount", heartCount));
    }

    @GetMapping("/products/author")
    public String authorProducts(@RequestParam Long authorId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                Model model) {
        try {
            Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("regTime").descending());
            Page<Product> productPage = productService.findProductsBySeller(author, pageable);
            
            int totalPages = productPage.getTotalPages();
            boolean hasPrevious = page > 0;
            boolean hasNext = page < totalPages - 1;
            
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("authorName", author.getName());
            model.addAttribute("authorId", authorId);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasPrevious", hasPrevious);
            model.addAttribute("hasNext", hasNext);
            
            return "product/authorProducts";
        } catch (Exception e) {
            log.error("작가별 상품 페이지 로드 중 오류 발생", e);
            return "redirect:/";
        }
    }

    /**
     * 포인트로 상품 구매 API
     */
    @PostMapping("/api/products/{id}/purchase/point")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> purchaseWithPoint(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
        }

        try {
            // 기존의 직접 포인트 차감/Order 생성 코드 삭제
            Order order = pointService.purchaseWithPoint(member.getId(), id, 1); // 단일 구매만 지원

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "포인트 구매가 완료되었습니다.",
                "orderId", order.getId(),
                "redirectUrl", "/api/orders/points/purchase/success?orderId=" + order.getId()
            ));

        } catch (IllegalArgumentException e) {
            log.error("포인트 구매 실패: 상품ID={}, 회원ID={}, 오류={}", id, member.getId(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("포인트 구매 중 오류: 상품ID={}, 회원ID={}, 오류={}", id, member.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "포인트 구매 중 오류가 발생했습니다."));
        }
    }

    /**
     * 구매 후 콘텐츠 조회 API
     */
    @GetMapping("/api/products/{id}/content")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductContent(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
        }

        try {
            Product product = productService.findProductById(id);
            
            // 구매 여부 확인
            if (!productReviewService.hasUserPurchasedProduct(member, product)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "구매 후에 콘텐츠를 확인할 수 있습니다."));
            }

            // Tiptap JSON을 HTML로 변환
            String contentHtml = "";
            String tiptapJson = product.getTiptapJson();
            if (tiptapJson != null && !tiptapJson.isEmpty()) {
                try {
                    TipTapDocument document = objectMapper.readValue(tiptapJson, TipTapDocument.class);
                    contentHtml = tipTapRenderer.render(document.getContent());
                } catch (JsonProcessingException e) {
                    log.error("Tiptap JSON 파싱 오류: {}", e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "contentHtml", contentHtml
            ));

        } catch (Exception e) {
            log.error("콘텐츠 조회 중 오류: 상품ID={}, 회원ID={}, 오류={}", id, member.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "콘텐츠 조회 중 오류가 발생했습니다."));
        }
    }

}
