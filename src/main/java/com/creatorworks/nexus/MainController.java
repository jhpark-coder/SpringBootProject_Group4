package com.creatorworks.nexus;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.creatorworks.nexus.product.dto.ProductPageResponse;
import com.creatorworks.nexus.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductService productService;

    @GetMapping(value = "/")
    public String main(Model model){
        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "regTime"));
        ProductPageResponse productPage = productService.findAllProducts(pageable);
        model.addAttribute("productPage", productPage);
        return "main";
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
