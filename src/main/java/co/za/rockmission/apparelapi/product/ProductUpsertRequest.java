package co.za.rockmission.apparelapi.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductUpsertRequest(
        @NotBlank String id,
        @NotBlank String name,
        @NotBlank String category,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        String imageUrl,
        String blurb,
        String art,
        String word,
        @NotEmpty List<@NotBlank String> sizes,
        @NotEmpty List<@NotBlank String> colors,
        boolean active
) {
}
