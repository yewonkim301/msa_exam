package com.sparta.msa_exam.product.products;


import com.sparta.msa_exam.product.core.Product;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    @CacheEvict(value = "productList", allEntries = true)  // 상품 목록 캐시를 모두 제거
    public ProductResponseDto createProduct(ProductRequestDto requestDto, String userId) {
        Product product = Product.createProduct(requestDto, userId);
        Product savedProduct = productRepository.save(product);
        return toResponseDto(savedProduct);
    }

    @Cacheable(value = "productList", unless = "#result.isEmpty()")  // 상품 목록 조회 시 캐시 적용
    public List<ProductResponseDto> getProducts(ProductSearchDto searchDto) {
        return productRepository.searchProducts(searchDto);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
            .filter(p -> p.getDeletedAt() == null) // 이렇게 데이터를 가져온 후에 필터링하는 것보다 가져올 때부터 필터링하는게 더 좋음
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Product not found or has been deleted"));
        return toResponseDto(product);
    }

    @Transactional
    public void reduceProductQuantity(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Product not found or has been deleted"));

        if (product.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Not enough quantity for product ID: " + productId);
        }

        product.reduceQuantity(quantity);
        productRepository.save(product);
    }

    private ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
            product.getProduct_id(),
            product.getName(),
            product.getSupply_price(),
            product.getQuantity()
        );
    }
}