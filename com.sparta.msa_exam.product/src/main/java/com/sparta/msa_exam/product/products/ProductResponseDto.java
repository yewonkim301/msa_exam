package com.sparta.msa_exam.product.products;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long product_id;
    private String name;
    private Integer supply_price;
    private Integer quantity;
}