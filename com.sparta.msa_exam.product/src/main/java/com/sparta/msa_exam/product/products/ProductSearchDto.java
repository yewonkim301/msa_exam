package com.sparta.msa_exam.product.products;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {
    private String name;
    private String description;
    private Double minPrice;
    private Double maxPrice;
    private Integer minQuantity;
    private Integer maxQuantity;

    @Override
    public String toString() {
        return "ProductSearchDto{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", minPrice=" + minPrice +
            ", maxPrice=" + maxPrice +
            ", minQuantity=" + minQuantity +
            ", maxQuantity=" + maxQuantity +
            '}';
    }
}