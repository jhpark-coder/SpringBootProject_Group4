package com.creatorworks.nexus.crawling.controller;

import com.creatorworks.nexus.crawling.entity.CrawlingHistory;
import com.creatorworks.nexus.crawling.repository.CrawlingHistoryRepository;
import com.creatorworks.nexus.crawling.service.LoudSourcingCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/crawling")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // ADMIN 권한이 있는 사용자만 접근 가능
public class CrawlingAdminController {

    private final LoudSourcingCrawlerService crawlerService;
    private final CrawlingHistoryRepository crawlingHistoryRepository;

    /**
     * 크롤링 관리 페이지
     */
    @GetMapping
    public String crawlingPage(Model model) {
        // 최근 크롤링 히스토리 조회
        List<CrawlingHistory> recentHistory = crawlingHistoryRepository.findTop10ByOrderByStartTimeDesc();
        model.addAttribute("recentHistory", recentHistory);
        return "admin/crawling";
    }

    /**
     * 크롤링 시작 API
     */
    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<String> startCrawling(@RequestParam("category") String category) {
        try {
            log.info("크롤링 시작 요청: {}", category);
            
            // @Async 어노테이션으로 비동기 실행
            crawlerService.startCrawling(category);
            
            return ResponseEntity.ok(category + " 카테고리 크롤링을 시작합니다. 완료까지 시간이 소요될 수 있습니다.");
            
        } catch (Exception e) {
            log.error("크롤링 시작 실패", e);
            return ResponseEntity.badRequest().body("크롤링 시작에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 크롤링 히스토리 조회 API
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<List<CrawlingHistory>> getCrawlingHistory() {
        try {
            List<CrawlingHistory> history = crawlingHistoryRepository.findTop10ByOrderByStartTimeDesc();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("크롤링 히스토리 조회 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 카테고리의 크롤링 히스토리 조회 API
     */
    @GetMapping("/history/{category}")
    @ResponseBody
    public ResponseEntity<List<CrawlingHistory>> getCrawlingHistoryByCategory(@PathVariable String category) {
        try {
            List<CrawlingHistory> history = crawlingHistoryRepository.findByCategoryOrderByStartTimeDesc(category);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("카테고리별 크롤링 히스토리 조회 실패: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }
} 