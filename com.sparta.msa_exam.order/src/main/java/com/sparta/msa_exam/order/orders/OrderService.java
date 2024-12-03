package com.sparta.msa_exam.order.orders;

import com.sparta.msa_exam.order.core.client.ProductClient;
import com.sparta.msa_exam.order.core.client.ProductResponseDto;
import com.sparta.msa_exam.order.core.domain.Order;
import com.sparta.msa_exam.order.core.enums.OrderStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId, boolean fail) {

        // 상품 API 호출 실패 유도
        if (fail) {
            log.error("상품 API 호출 실패를 유도합니다.");
            throw new RuntimeException("상품 API 호출 실패"); // 인위적으로 실패 시킴
        }

        return processOrder(requestDto, userId);
    }

    private OrderResponseDto processOrder(OrderRequestDto requestDto, String userId) {
        List<Long> orderItemIds = requestDto.getProduct_ids();
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order item IDs cannot be null or empty.");
        }

        // 수량 체크
        for (Long productId : orderItemIds) {
            ProductResponseDto product = productClient.getProduct(productId);
            log.info("############################ Product 수량 확인 : " + product.getQuantity());

            // quantity가 null일 경우 0으로 처리하거나 예외를 던짐
            Integer quantity = product.getQuantity();
            if (quantity == null || quantity < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " is out of stock.");
            }
        }

        for (Long productId : orderItemIds) {
            productClient.reduceProductQuantity(productId, 1);
        }

        Order order = Order.createOrder(orderItemIds, userId);
        Order savedOrder = orderRepository.save(order);

        return toResponseDto(savedOrder);
    }



    // Fallback 메서드
    public OrderResponseDto createOrderFallback(OrderRequestDto requestDto, String userId, boolean fail, Throwable throwable) {
        log.error("Product API 호출 실패: {}", throwable.getMessage());
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "잠시 후에 주문 추가를 요청 해주세요.");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
    // unless: 조건에 따라 캐싱 제외. 예: 결과가 null이면 캐싱하지 않음.
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));
        return toResponseDto(order);
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponseDto updateOrder(Long orderId, OrderRequestDto requestDto,String userId) {
        // 주문 가져오기
        Order order = orderRepository.findById(orderId)
            .filter(o -> o.getDeletedAt() == null)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));

        // 상품 목록 조회
        List<ProductResponseDto> productList = productClient.getProducts();
        log.info("Fetched product list: {}", productList);

        // 유효한 상품 ID만 필터링
        List<Long> validProductIds = new ArrayList<>(requestDto.getProduct_ids().stream()
            .filter(productId -> productList.stream().anyMatch(product -> product.getProduct_id().equals(productId)))
            .toList());

        if (validProductIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid products to add to the order.");
        }

        // 주문 업데이트
        order.updateOrder(validProductIds, userId, OrderStatus.valueOf(requestDto.getStatus()));
        Order updatedOrder = orderRepository.save(order);

        return toResponseDto(updatedOrder);
    }

    private OrderResponseDto toResponseDto(Order order) {
        return new OrderResponseDto(
                order.getOrder_id(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getCreatedBy(),
                order.getUpdatedAt(),
                order.getUpdatedBy(),
                order.getProduct_ids()
        );
    }
}