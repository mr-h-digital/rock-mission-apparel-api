package co.za.rockmission.apparelapi.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderItemRequest(
        @NotBlank String productId,
        @NotBlank String size,
        @NotBlank String color,
        @Min(1) int qty) {
}
