package co.za.rockmission.apparelapi.product;

import java.math.BigDecimal;

public record ProductDto(String id, String name, String category, BigDecimal price) {
    static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getCategory(), product.getPrice());
    }
}
