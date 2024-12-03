package com.sparta.msa_exam.order.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    // 주문 추가 API
    @PostMapping
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto orderRequestDto,
        @RequestHeader(value = "X-User-Id", required = true) String userId,
        @RequestHeader(value = "X-Role", required = true) String role,
        @RequestParam(value = "fail", required = false) Boolean fail) {

        // fail 파라미터가 null일 경우 false로 기본값을 설정
        if (fail == null) {
            fail = false;
        }

        return orderService.createOrder(orderRequestDto, userId, fail);
    }

    // 주문 단건 조회 API
    @GetMapping("/{orderId}")
    public OrderResponseDto getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    // 주문에 상품을 추가하는 API
    @PutMapping("/{orderId}")
    public OrderResponseDto updateOrder(@PathVariable Long orderId,
                                        @RequestBody OrderRequestDto orderRequestDto,
                                        @RequestHeader(value = "X-User-Id", required = true) String userId,
                                        @RequestHeader(value = "X-Role", required = true) String role) {
        return orderService.updateOrder(orderId, orderRequestDto, userId);
    }

}
