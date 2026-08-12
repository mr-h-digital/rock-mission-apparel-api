package co.za.rockmission.apparelapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotUsernameRequest(
        @Email @NotBlank String email
) {
}
