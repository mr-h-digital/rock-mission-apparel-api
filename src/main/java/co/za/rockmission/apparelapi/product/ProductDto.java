package co.za.rockmission.apparelapi.product;

import java.util.List;
import java.math.BigDecimal;

public record ProductDto(
        String id,
        String name,
        String category,
        BigDecimal price,
        String imageUrl,
        String blurb,
        String art,
        String word,
        List<String> sizes,
        List<String> colors,
        List<ProductInventoryDto> inventory,
        boolean active) {

    static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                product.getBlurb(),
                product.getArt(),
                product.getWord(),
                csvToList(product.getSizesCsv()),
                csvToList(product.getColorsCsv()),
                product.getInventory().stream().map(ProductInventoryDto::from).toList(),
                product.isActive()
        );
    }

    private static List<String> csvToList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
