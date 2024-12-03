package com.sparta.msa_exam.product.products;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productRequestDto,
        @RequestHeader(value = "X-User-Id", required = true) String userId,
        @RequestHeader(value = "X-Role", required = true) String role) {
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. User role is not MANAGER.");
        }
        ProductResponseDto productResponse = productService.createProduct(productRequestDto, userId);
        return ResponseEntity.ok()
            .header("Server-Port", String.valueOf(System.getProperty("server.port", "Unknown Port")))
            .body(productResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getProducts(ProductSearchDto searchDto) {
        List<ProductResponseDto> productList = productService.getProducts(searchDto);
        return ResponseEntity.ok()
            .header("Server-Port", String.valueOf(System.getProperty("server.port", "Unknown Port")))
            .body(productList);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long productId) {
        ProductResponseDto productResponse = productService.getProductById(productId);
        return ResponseEntity.ok()
            .header("Server-Port", String.valueOf(System.getProperty("server.port", "Unknown Port")))
            .body(productResponse);
    }

    @GetMapping("/{id}/reduceQuantity")
    public ResponseEntity<Void> reduceProductQuantity(@PathVariable Long id, @RequestParam int quantity) {
        productService.reduceProductQuantity(id, quantity);
        return ResponseEntity.ok()
            .header("Server-Port", String.valueOf(System.getProperty("server.port", "Unknown Port")))
            .build();
    }
}
