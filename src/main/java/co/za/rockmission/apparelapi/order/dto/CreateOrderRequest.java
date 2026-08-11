package co.za.rockmission.apparelapi.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @Valid CustomerDto customer,
        @NotEmpty @Valid List<OrderItemRequest> items) {
}
