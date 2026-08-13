package co.za.rockmission.apparelapi.product;

public record ProductInventoryDto(
        String size,
        String color,
        int quantity,
        int reserved,
        int available
) {
    static ProductInventoryDto from(ProductInventory inventory) {
        return new ProductInventoryDto(
                inventory.getSize(),
                inventory.getColor(),
                inventory.getStockOnHand(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity());
    }
}
