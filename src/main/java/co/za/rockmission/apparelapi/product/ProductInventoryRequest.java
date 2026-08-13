package co.za.rockmission.apparelapi.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductInventoryRequest(
        @NotBlank String size,
        @NotBlank String color,
        @Min(0) int quantity
) {
}
