package co.za.rockmission.apparelapi.auth.dto;

public record AuthResponse(String token, UserResponse user) {
}
