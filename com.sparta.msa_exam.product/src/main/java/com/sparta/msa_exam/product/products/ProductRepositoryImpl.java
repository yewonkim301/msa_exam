package com.sparta.msa_exam.product.products;

import static com.sparta.msa_exam.product.core.QProduct.product;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.msa_exam.product.core.Product;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductResponseDto> searchProducts(ProductSearchDto searchDto) {
        // QueryDSL 쿼리 실행
        List<Product> results = queryFactory
            .selectFrom(product)
            .where(
                nameContains(searchDto.getName()),
                priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()),
                quantityBetween(searchDto.getMinQuantity(), searchDto.getMaxQuantity())
            )
            .fetch();

        // Product 엔티티를 ProductResponseDto로 변환
        return results.stream()
            .map(Product::toResponseDto)
            .collect(Collectors.toList());
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.containsIgnoreCase(name) : null;
    }


    private BooleanExpression priceBetween(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return product.supply_price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return product.supply_price.goe(minPrice);
        } else if (maxPrice != null) {
            return product.supply_price.loe(maxPrice);
        } else {
            return null;
        }
    }

    private BooleanExpression quantityBetween(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null) {
            return product.quantity.between(minQuantity, maxQuantity);
        } else if (minQuantity != null) {
            return product.quantity.goe(minQuantity);
        } else if (maxQuantity != null) {
            return product.quantity.loe(maxQuantity);
        } else {
            return null;
        }
    }
}