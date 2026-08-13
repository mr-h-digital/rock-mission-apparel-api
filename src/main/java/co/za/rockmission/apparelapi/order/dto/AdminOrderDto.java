package co.za.rockmission.apparelapi.order.dto;

import co.za.rockmission.apparelapi.order.Order;
import co.za.rockmission.apparelapi.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminOrderDto(
        UUID id,
        OrderStatus status,
        String firstName,
        String lastName,
        String email,
        String phone,
        BigDecimal totalAmount,
        String payfastPaymentId,
        Instant createdAt,
        Instant updatedAt,
        List<AdminOrderItemDto> items
) {
    public static AdminOrderDto from(Order order) {
        return new AdminOrderDto(
                order.getId(),
                order.getStatus(),
                order.getFirstName(),
                order.getLastName(),
                order.getEmail(),
                order.getPhone(),
                order.getTotalAmount(),
                order.getPayfastPaymentId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(AdminOrderItemDto::from).toList());
    }
}
