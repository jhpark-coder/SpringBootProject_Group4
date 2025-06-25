package com.creatorworks.nexus.product.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.product.dto.ProductSaveRequest;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @Service: 이 클래스가 비즈니스 로직을 처리하는 서비스 계층의 컴포넌트임을 Spring에 알립니다.
 * @Transactional: 이 클래스의 모든 public 메소드는 하나의 트랜잭션 단위로 동작합니다.
 *                 메소드 실행 중 예외가 발생하면, 데이터베이스 작업을 모두 롤백하여 데이터 일관성을 유지합니다.
 * @AllArgsConstructor: Lombok 어노테이션으로, 이 클래스의 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 */
@Service
@Transactional
@AllArgsConstructor
public class ProductService {

    // 데이터베이스와 상호작용하는 ProductRepository를 주입받습니다.
    private final ProductRepository productRepository;

    /**
     * 페이징 처리된 모든 상품 목록을 조회합니다.
     * @Cacheable: Spring의 캐싱 기능을 활성화합니다.
     *             - value = "products": 'products'라는 이름의 캐시에 결과를 저장합니다.
     *             - key = "#pageable": 메소드의 파라미터인 'pageable' 객체를 캐시의 키로 사용합니다.
     *             동일한 pageable 정보(페이지 번호, 사이즈 등)로 요청이 다시 오면,
     *             실제 메소드를 실행하지 않고 캐시에 저장된 결과를 즉시 반환하여 성능을 향상시킵니다.
     * @param pageable 페이징 및 정렬 정보.
     * @return 페이징된 상품(Product) 목록.
     */
    @Cacheable(value = "products", key = "#pageable")
    public Page<Product> findAllProducts(Pageable pageable) {
        // 이 로그는 캐시에 없는 새로운 페이지를 요청할 때만 출력됩니다.
        System.out.println("DB에서 상품 목록을 조회합니다. page=" + pageable.getPageNumber());
        // ProductRepository를 통해 데이터베이스에서 상품 목록을 조회합니다.
        return productRepository.findAll(pageable);
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
     * 새로운 상품을 데이터베이스에 저장합니다.
     * @param request 상품 저장에 필요한 데이터를 담은 DTO(Data Transfer Object).
     * @return 데이터베이스에 저장된 후의 상품(Product) 객체 (생성된 ID 포함).
     */
    public Product saveProduct(ProductSaveRequest request) {
        // (디버깅용 로그)
        System.out.println("=== 상품 저장 요청 데이터 ===");
        System.out.println("이름: " + request.getName());
        System.out.println("가격: " + request.getPrice());
        System.out.println("TiptapJson 길이: " + (request.getTiptapJson() != null ? request.getTiptapJson().length() : "null"));
        System.out.println("TiptapJson 내용: " + request.getTiptapJson());
        System.out.println("HtmlBackup 길이: " + (request.getHtmlBackup() != null ? request.getHtmlBackup().length() : "null"));
        System.out.println("배경색: " + request.getBackgroundColor());
        System.out.println("폰트: " + request.getFontFamily());
        System.out.println("===========================");
        
        // 1. 새로운 Product 엔티티 객체를 생성합니다.
        Product product = new Product();
        // 2. 요청 DTO(request)로부터 받은 데이터로 Product 엔티티의 각 필드를 채웁니다.
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setDescription(request.getHtmlBackup());
        product.setTiptapJson(request.getTiptapJson());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());
        product.setPrice(request.getPrice());
        // 3. 채워진 엔티티 객체를 Repository를 통해 데이터베이스에 저장(save)하고, 그 결과를 반환합니다.
        return productRepository.save(product);
    }

    /**
     * 기존 상품의 정보를 수정합니다.
     * @param id 수정할 상품의 ID.
     * @param request 상품 수정에 필요한 데이터를 담은 DTO.
     * @return 정보가 수정된 후의 상품(Product) 객체.
     */
    public Product updateProduct(Long id, ProductSaveRequest request) {
        // (디버깅용 로그)
        System.out.println("Updating product with ID: " + id);
        
        // 1. 먼저 ID를 사용하여 데이터베이스에서 기존 상품 정보를 찾아옵니다. (영속성 컨텍스트에 로드됨)
        Product product = findProductById(id);
        // 2. 찾아온 엔티티 객체의 필드 값을 새로운 데이터로 변경합니다.
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrimaryCategory(request.getPrimaryCategory());
        product.setSecondaryCategory(request.getSecondaryCategory());
        product.setDescription(request.getHtmlBackup());
        product.setTiptapJson(request.getTiptapJson());
        product.setBackgroundColor(request.getBackgroundColor());
        product.setFontFamily(request.getFontFamily());
        product.setPrice(request.getPrice());
        
        try {
            // 3. @Transactional에 의해, 메소드가 종료될 때 변경된 엔티티(product)가 자동으로 감지되어
            //    데이터베이스에 업데이트 쿼리가 실행됩니다. (이를 '더티 체킹'이라고 합니다)
            //    productRepository.save(product)를 명시적으로 호출해도 동일하게 동작합니다.
            Product updated = productRepository.save(product);
            System.out.println("Product updated successfully with ID: " + updated.getId());
            return updated;
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
