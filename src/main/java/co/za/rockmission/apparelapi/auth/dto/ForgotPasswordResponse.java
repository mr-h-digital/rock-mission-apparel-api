package co.za.rockmission.apparelapi.auth.dto;

public record ForgotPasswordResponse(String message, String resetToken, String resetUrl) {
}
