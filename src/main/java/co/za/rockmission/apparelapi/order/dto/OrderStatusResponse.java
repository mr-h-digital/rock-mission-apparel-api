package co.za.rockmission.apparelapi.order.dto;

import co.za.rockmission.apparelapi.order.Order;
import co.za.rockmission.apparelapi.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderStatusResponse(
        UUID orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt,
        String payfastPaymentId
) {
    public static OrderStatusResponse from(Order order) {
        return new OrderStatusResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPayfastPaymentId()
        );
    }
}
