package com.creatorworks.nexus;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.product.dto.ProductDto;
import com.creatorworks.nexus.product.service.MainPageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainPageService mainPageService;

    @GetMapping(value = "/")
    public String main(Principal principal, Model model){
        return "main";
    }

    @GetMapping("/api/main/recommendations")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> getMainRecommendations(Principal principal) {
        String userEmail = (principal != null) ? principal.getName() : null;

        List<ProductDto> products = mainPageService.getProductsForMainPage(userEmail);

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
