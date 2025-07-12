package com.creatorworks.nexus.crawling.service;

import com.creatorworks.nexus.crawling.config.CrawlingConfig;
import com.creatorworks.nexus.crawling.dto.CrawledContestDto;
import com.creatorworks.nexus.crawling.entity.CrawlingHistory;
import com.creatorworks.nexus.crawling.repository.CrawlingHistoryRepository;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoudSourcingCrawlerService {

    private final ProductRepository productRepository;
    private final CrawlingHistoryRepository crawlingHistoryRepository;
    private final MemberRepository memberRepository;
    private final CrawlingConfig crawlingConfig;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 크롤링을 시작하는 메인 메소드
     */
    @Async
    public void startCrawling(String category) {
        long startTime = System.currentTimeMillis();
        CrawlingHistory history = CrawlingHistory.builder()
                .category(category)
                .build();
        
        Set<CrawledContestDto> finalResults = new LinkedHashSet<>();
        WebDriver driver = null;

        try {
            log.info("크롤링 시작: {}", category);
            
            // 크롤링 디렉토리 생성
            Path crawlingDir = Paths.get(uploadDir, "crawled_images");
            Files.createDirectories(crawlingDir);
            
            // WebDriverManager로 ChromeDriver 설정
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver(configureChromeOptions());

            if (!performLogin(driver)) {
                throw new RuntimeException("로그인 실패");
            }

            Set<String> contestLinks = collectContestLinks(driver, category);

            if (!contestLinks.isEmpty()) {
                finalResults = crawlContestDetails(driver, contestLinks, crawlingDir);
            }
            
            // 크롤링 결과를 실제 상품으로 변환하여 DB에 저장
            saveAsProducts(finalResults);

            history.success(finalResults.size(), "크롤링 및 상품 변환 성공");

        } catch (Exception e) {
            log.error("크롤링 중 오류 발생", e);
            history.fail("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
            crawlingHistoryRepository.save(history);
            long endTime = System.currentTimeMillis();
            log.info("크롤링 작업 완료. 소요시간: {}초", (endTime - startTime) / 1000);
        }
    }

    /**
     * Chrome 옵션 설정
     */
    private ChromeOptions configureChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 헤드리스 모드
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        return options;
    }

    /**
     * 로그인 수행
     */
    private boolean performLogin(WebDriver driver) {
        try {
            driver.get(crawlingConfig.getLoud().getLoginUrl());
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(crawlingConfig.getTimeout().getSeconds()));
            
            // 로그인 폼 입력
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
                By.name(crawlingConfig.getSelectors().getUsernameField())
            ));
            WebElement passwordField = driver.findElement(By.name(crawlingConfig.getSelectors().getPasswordField()));
            
            usernameField.sendKeys(crawlingConfig.getLoud().getUsername());
            passwordField.sendKeys(crawlingConfig.getLoud().getPassword());
            
            // 로그인 버튼 클릭
            WebElement loginButton = driver.findElement(By.cssSelector(crawlingConfig.getSelectors().getLoginButton()));
            loginButton.click();
            
            // 로그인 성공 확인
            wait.until(ExpectedConditions.urlContains("dashboard"));
            return true;
            
        } catch (Exception e) {
            log.error("로그인 실패", e);
            return false;
        }
    }

    /**
     * 콘테스트 링크 수집
     */
    private Set<String> collectContestLinks(WebDriver driver, String category) {
        Set<String> links = new LinkedHashSet<>();
        try {
            String categoryUrl = crawlingConfig.getLoud().getContestUrl() + "?category=" + category;
            driver.get(categoryUrl);
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(crawlingConfig.getTimeout().getSeconds()));
            List<WebElement> contestElements = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(crawlingConfig.getSelectors().getContestItem()))
            );
            
            for (WebElement element : contestElements) {
                String href = element.getAttribute("href");
                if (href != null && href.contains("/contest/")) {
                    links.add(href);
                }
            }
            
        } catch (Exception e) {
            log.error("콘테스트 링크 수집 실패", e);
        }
        return links;
    }

    /**
     * 콘테스트 상세 정보 크롤링
     */
    private Set<CrawledContestDto> crawlContestDetails(WebDriver driver, Set<String> links, Path baseDir) {
        Set<CrawledContestDto> results = new LinkedHashSet<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(crawlingConfig.getTimeout().getSeconds()));
        
        for (String link : links) {
            try {
                driver.get(link);
                
                // 콘테스트 제목 추출 (상세 페이지)
                String title = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(crawlingConfig.getSelectors().getContestTitle())
                )).getText();
                
                // 태그 추출
                List<String> tags = new ArrayList<>();
                List<WebElement> tagElements = driver.findElements(By.cssSelector(crawlingConfig.getSelectors().getTagItem()));
                for (WebElement tagElement : tagElements) {
                    String tagText = tagElement.getText().trim();
                    if (!tagText.isEmpty()) {
                        tags.add(tagText);
                    }
                }
                
                // 우승자 컨테이너에서 작가명과 썸네일 추출
                String author = "";
                String thumbnailPath = "";
                
                try {
                    // 우승자 컨테이너들 찾기
                    List<WebElement> winnerContainers = driver.findElements(
                        By.cssSelector(crawlingConfig.getSelectors().getWinnerContainer())
                    );
                    
                    // 1등 작품 찾기 (badge 텍스트로 필터링)
                    WebElement firstPlaceContainer = null;
                    for (WebElement container : winnerContainers) {
                        try {
                            List<WebElement> badges = container.findElements(By.cssSelector("label.badge"));
                            for (WebElement badge : badges) {
                                if (badge.getText().contains("1등") || badge.getText().contains("1st")) {
                                    firstPlaceContainer = container;
                                    break;
                                }
                            }
                            if (firstPlaceContainer != null) break;
                        } catch (Exception e) {
                            // 개별 컨테이너 처리 중 오류 무시
                            continue;
                        }
                    }
                    
                    if (firstPlaceContainer != null) {
                        // 작가명 추출
                        WebElement authorElement = firstPlaceContainer.findElement(
                            By.cssSelector(crawlingConfig.getSelectors().getAuthorName())
                        );
                        author = authorElement.getText();
                        
                        // 썸네일 이미지 다운로드
                        WebElement thumbnailElement = firstPlaceContainer.findElement(
                            By.cssSelector(crawlingConfig.getSelectors().getThumbnailImg())
                        );
                        String imageUrl = thumbnailElement.getAttribute("src");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            thumbnailPath = downloadImage(imageUrl, baseDir, author + "_" + title + "_thumb");
                        }
                        
                        log.info("1등 작품 발견: {} - {}", author, title);
                    } else {
                        log.warn("1등 작품을 찾을 수 없음: {}", link);
                        author = "Unknown Artist";
                    }
                } catch (Exception e) {
                    log.warn("우승자 정보 추출 실패, 기본 정보로 진행: {}", e.getMessage());
                    // 우승자 정보가 없으면 기본 정보로 진행
                    author = "Unknown Artist";
                }
                
                // 상세 이미지들 다운로드
                List<String> detailImagePaths = new ArrayList<>();
                List<WebElement> detailImages = driver.findElements(By.cssSelector(crawlingConfig.getSelectors().getDetailImages()));
                for (int i = 0; i < detailImages.size(); i++) {
                    String imageUrl = detailImages.get(i).getAttribute("src");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        String imagePath = downloadImage(imageUrl, baseDir, author + "_" + title + "_detail_" + i);
                        if (imagePath != null) {
                            detailImagePaths.add(imagePath);
                        }
                    }
                }
                
                // 최소한의 필수 정보가 있는 경우만 결과에 추가
                if (!author.isEmpty() && !title.isEmpty()) {
                    CrawledContestDto dto = new CrawledContestDto(author, title, tags, thumbnailPath, detailImagePaths);
                    results.add(dto);
                    
                    log.info("크롤링 완료: {} - {} (태그: {}, 이미지: {}개)", 
                            author, title, tags.size(), detailImagePaths.size());
                } else {
                    log.warn("필수 정보 부족으로 건너뜀: {}", link);
                }
                
                // 요청 간 지연 시간
                Thread.sleep(crawlingConfig.getDelayBetweenRequestsMs());
                
            } catch (Exception e) {
                log.error("상세 정보 크롤링 실패: {}", link, e);
            }
        }
        
        return results;
    }

    /**
     * 이미지 다운로드
     */
    private String downloadImage(String imageUrl, Path baseDir, String fileName) {
        try {
            String extension = getImageExtension(imageUrl);
            String safeFileName = sanitizeFileName(fileName) + "." + extension;
            Path imagePath = baseDir.resolve(safeFileName);
            
            // 이미지 다운로드 및 저장
            try (var inputStream = new URL(imageUrl).openStream()) {
                Files.copy(inputStream, imagePath);
            }
            
            return imagePath.toString();
            
        } catch (IOException e) {
            log.error("이미지 다운로드 실패: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 이미지 확장자 추출
     */
    private String getImageExtension(String imageUrl) {
        if (imageUrl.contains(".")) {
            String extension = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
            if (extension.length() <= 4) {
                return extension;
            }
        }
        return "jpg";
    }

    /**
     * 파일명 정리
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9가-힣]", "_")
                      .replaceAll("_+", "_")
                      .substring(0, Math.min(fileName.length(), 100));
    }

    /**
     * 크롤링된 데이터를 Product 엔티티로 변환하고 저장
     */
    @Transactional
    public void saveAsProducts(Set<CrawledContestDto> contestDtos) {
        for (CrawledContestDto dto : contestDtos) {
            // 이미 존재하는 상품인지 확인
            if (productRepository.existsByNameAndSeller_Name(dto.title(), dto.author())) {
                log.info("이미 존재하는 상품입니다. 건너뜁니다: {}", dto.title());
                continue;
            }
            
            // 작가 정보 조회 또는 생성
            Member seller = findOrCreateSeller(dto.author());
            
            // Product 엔티티 생성
            Product product = Product.builder()
                    .seller(seller)
                    .name(dto.title())
                    .description(String.join(", ", dto.tags()))
                    .price(0L) // 초기 가격은 0원으로 설정
                    .imageUrl(dto.thumbnailPath())
                    .tiptapJson(convertImagesToJson(dto.detailImagePaths()))
                    .primaryCategory("크롤링")
                    .secondaryCategory("로고-브랜딩")
                    .crawledAuthorName(dto.author())
                    .crawledSourceUrl("") // 원본 URL은 나중에 추가 가능
                    .crawledTags(String.join(", ", dto.tags()))
                    .build();

            productRepository.save(product);
            log.info("새 상품 저장 완료: {}", product.getName());
        }
    }

    /**
     * 작가 정보 조회 또는 생성
     */
    private Member findOrCreateSeller(String authorName) {
        // 기존 작가가 있는지 확인 (이메일로 검색)
        String tempEmail = authorName + "@crawled.com";
        Member existingMember = memberRepository.findByEmail(tempEmail);
        if (existingMember != null) {
            return existingMember;
        }
        
        // 새로운 작가 생성
        Member newMember = Member.builder()
                .email(authorName + "@crawled.com") // 임시 이메일
                .password("crawled_password") // 임시 비밀번호
                .name(authorName)
                .gender("기타")
                .birthYear("1990")
                .birthMonth("01")
                .birthDay("01")
                .role(com.creatorworks.nexus.member.constant.Role.USER)
                .point(0)
                .build();
        
        return memberRepository.save(newMember);
    }

    /**
     * 상세 이미지 경로 리스트를 TipTap JSON 형식으로 변환
     */
    private String convertImagesToJson(List<String> imagePaths) {
        StringBuilder contentJson = new StringBuilder("{\"type\":\"doc\",\"content\":[");
        
        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i).replace("\\", "/");
            // uploads 디렉토리를 기준으로 상대 경로 생성
            String relativePath = imagePath.replace(uploadDir, "uploads");
            
            contentJson.append(String.format(
                "{\"type\":\"image\",\"attrs\":{\"src\":\"/%s\",\"alt\":null,\"title\":null}}", 
                relativePath
            ));
            
            if (i < imagePaths.size() - 1) {
                contentJson.append(",");
            }
        }
        
        contentJson.append("]}");
        return contentJson.toString();
    }
} 