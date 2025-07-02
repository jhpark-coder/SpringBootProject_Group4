package com.creatorworks.nexus;

import com.creatorworks.nexus.product.service.MainPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.creatorworks.nexus.product.dto.ProductDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    // --- ★★★ 변경된 부분 ★★★ ---
    // ProductService 대신 MainPageService를 주입받습니다.
    private final MainPageService mainPageService;

//    private final ProductService productService; 원본

    @GetMapping(value = "/")
    public String main(Principal principal, Model model){

//        // 1. 로그인 상태 확인
//        String userEmail = (principal != null) ? principal.getName() : null;
//
//        // 2. MainPageService를 호출하여 추천 상품 리스트를 가져옵니다.
//        List<ProductDto> products = mainPageService.getProductsForMainPage(userEmail);
//
//        // 3. 모델에 'products'라는 이름으로 담아 View로 전달합니다.
//        //    (기존 'productPage' 대신 'products'를 사용)
//        model.addAttribute("products", products);
//
//        //그리드 수정 중 아래는 원본
////        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "regTime"));
////        ProductPageResponse productPage = productService.findAllProducts(pageable);
////        model.addAttribute("productPage", productPage);
        return "main";
    }
    @GetMapping("/api/main/recommendations")
    @ResponseBody // HTML이 아닌 JSON 데이터를 반환
    public ResponseEntity<List<ProductDto>> getMainRecommendations(Principal principal) {
        // 로그인 상태 확인
        String userEmail = (principal != null) ? principal.getName() : null;

        // 서비스를 호출하여 추천 상품 리스트를 가져옵니다.
        List<ProductDto> products = mainPageService.getProductsForMainPage(userEmail);

        // JSON 데이터와 HTTP 상태코드(200 OK)를 함께 반환합니다.
        return ResponseEntity.ok(products);
    }

    @GetMapping("/main-content")
    public String getMainContent() {
        return "fragment/main_content";
    }

    @GetMapping("/sentinel")
    public String sentinelPage() {
        return "fragment/sentinel";
    }
}
