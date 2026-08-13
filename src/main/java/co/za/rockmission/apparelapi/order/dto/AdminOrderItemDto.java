package co.za.rockmission.apparelapi.order.dto;

import co.za.rockmission.apparelapi.order.OrderItem;

public record AdminOrderItemDto(
        String productId,
        String productName,
        String size,
        String color,
        int quantity,
        java.math.BigDecimal unitPrice
) {
    public static AdminOrderItemDto from(OrderItem item) {
        return new AdminOrderItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getSize(),
                item.getColor(),
                item.getQty(),
                item.getUnitPrice());
    }
}
