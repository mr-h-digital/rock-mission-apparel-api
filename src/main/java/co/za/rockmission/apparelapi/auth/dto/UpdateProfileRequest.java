package co.za.rockmission.apparelapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String province,
        String postalCode,
        String country
) {
}
