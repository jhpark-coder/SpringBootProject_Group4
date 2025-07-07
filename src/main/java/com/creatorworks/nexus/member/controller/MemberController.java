package com.creatorworks.nexus.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.creatorworks.nexus.member.dto.EmailAuthRequestDto;
import com.creatorworks.nexus.member.dto.MemberFormDto;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.service.MemberService;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.service.PointService;
import java.security.Principal;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductHeart;
import com.creatorworks.nexus.product.repository.ProductHeartRepository;
import com.creatorworks.nexus.member.service.MemberFollowService;
import com.creatorworks.nexus.product.repository.ProductRepository;
import com.creatorworks.nexus.product.service.ProductService;
import com.creatorworks.nexus.product.dto.ProductDto;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final PointService pointService;
    private final ProductHeartRepository productHeartRepository;
    private final MemberFollowService memberFollowService;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @GetMapping("/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }
    @GetMapping("/point")
    public String MemberPoint(Model model, Principal principal) {
        // GlobalModelAttributes에서 자동으로 currentPoint가 추가됨
        return "member/point";
    }

    @GetMapping("/subscription")
    public String subscription(Model model, Principal principal,
                              @RequestParam(required = false) String author,
                              @RequestParam(required = false) Long productId) {
        // GlobalModelAttributes에서 자동으로 currentPoint가 추가됨
        
        // URL 파라미터에서 작가 정보와 상품 ID를 모델에 추가
        model.addAttribute("authorName", author != null ? author : "작가명");
        model.addAttribute("productId", productId);
        
        return "member/subscription";
    }

    @GetMapping("/liked-products")
    public String likedProducts(Model model, Principal principal,
                               @RequestParam(defaultValue = "0") int page) {
        if (principal == null) {
            return "redirect:/members/login";
        }
        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }

        Pageable pageable = PageRequest.of(page, 12); // 한 페이지당 12개
        Page<Product> likedProductsPage = productService.getLikedProducts(member.getId(), pageable);

        // 팔로우 상태 정보 추가
        List<ProductDto> likedProductsWithFollow = likedProductsPage.getContent().stream()
                .map(product -> {
                    boolean isFollowing = false;
                    if (product.getSeller() != null) {
                        isFollowing = memberFollowService.isFollowing(member.getId(), product.getSeller().getId());
                    }
                    return new ProductDto(product, isFollowing);
                })
                .collect(Collectors.toList());

        model.addAttribute("likedProducts", likedProductsWithFollow);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", likedProductsPage.getTotalPages());
        model.addAttribute("hasNext", likedProductsPage.hasNext());
        model.addAttribute("hasPrevious", likedProductsPage.hasPrevious());

        return "member/likedProducts";
    }

    @GetMapping("/following-products")
    public String followingProducts(Model model, Principal principal,
                                  @RequestParam(defaultValue = "0") int page) {
        if (principal == null) {
            return "redirect:/members/login";
        }
        
        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            return "redirect:/members/login";
        }
        
        try {
            // 페이징 처리
            Pageable pageable = PageRequest.of(page, 12); // 한 페이지당 12개 작품
            
            // 팔로우한 작가들의 작품들을 가져오기
            Page<Product> followingProducts = productService.getFollowingProducts(principal.getName(), pageable);
            
            // 팔로우 상태 정보 추가
            List<ProductDto> followingProductsWithFollow = followingProducts.getContent().stream()
                    .map(product -> {
                        boolean isFollowing = false;
                        if (product.getSeller() != null) {
                            isFollowing = memberFollowService.isFollowing(member.getId(), product.getSeller().getId());
                        }
                        return new ProductDto(product, isFollowing);
                    })
                    .collect(Collectors.toList());
            
            model.addAttribute("followingProducts", followingProductsWithFollow);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", followingProducts.getTotalPages());
            model.addAttribute("totalElements", followingProducts.getTotalElements());
            model.addAttribute("hasNext", followingProducts.hasNext());
            model.addAttribute("hasPrevious", followingProducts.hasPrevious());
            
            // 팔로우한 작가 수
            long followingCount = memberFollowService.getFollowingCount(principal.getName());
            model.addAttribute("followingCount", followingCount);
            
        } catch (Exception e) {
            model.addAttribute("error", "팔로우한 작품 목록을 불러오는데 실패했습니다.");
        }
        
        return "member/followingProducts";
    }

    // 구독 결제 API
    @PostMapping("/api/subscription/start")
    public ResponseEntity<Map<String, Object>> startSubscription(@RequestBody Map<String, Object> request, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Member member = memberRepository.findByEmail(principal.getName());
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
            }

            String plan = (String) request.get("plan");
            Integer price = (Integer) request.get("price");
            Integer bonusPoints = (Integer) request.get("bonusPoints");
            String impUid = (String) request.get("impUid");
            String merchantUid = (String) request.get("merchantUid");

            // 구독 정보 저장 (실제 구현에서는 구독 엔티티에 저장)
            // 여기서는 포인트 보너스만 지급
            if (bonusPoints > 0) {
                pointService.addPoints(member.getId(), bonusPoints.longValue(), "구독 보너스 포인트");
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "구독이 성공적으로 시작되었습니다.",
                "plan", plan,
                "bonusPoints", bonusPoints
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "구독 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    @PostMapping("/api/new")
    @ResponseBody
    public ResponseEntity<?> registerMember(@Valid @RequestBody MemberFormDto memberFormDto, BindingResult bindingResult){
        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("globalError", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.OK);
    }

    @PostMapping("/email-auth")
    public ResponseEntity<String> sendAuthEmail(@RequestBody EmailAuthRequestDto requestDto) {
        try {
            memberService.sendAuthEmail(requestDto.getEmail());
            System.out.println("인증메일이 발송되었습니다.");
            return new ResponseEntity<>("인증 메일이 발송되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("메일 발송에 실패했습니다.");
            return new ResponseEntity<>("메일 발송에 실패했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/email-verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailAuthRequestDto requestDto) {
        boolean isVerified = memberService.verifyEmail(requestDto);
        if (isVerified) {
            return new ResponseEntity<>("이메일 인증이 성공하였습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("인증 코드가 일치하지 않거나 만료되었습니다.", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/login")
    public String login(@RequestParam(value = "message", required = false) String message, 
                       Model model, HttpServletRequest request) {
        if (message != null) {
            model.addAttribute("message", message);
            // 권한 업데이트 메시지인 경우 기존 세션 무효화
            if (message.contains("권한이 업데이트되었습니다")) {
                request.getSession().invalidate();
            }
        }
        return "member/loginForm";
    }
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg","이메일(ID) 또는 비밀번호를 확인해주세요");
        return "member/loginForm";
    }

    @GetMapping("/seller-register")
    public String sellerRegisterPage() {
        return "member/seller-register";
    }
}
