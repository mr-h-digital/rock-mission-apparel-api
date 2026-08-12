package co.za.rockmission.apparelapi.auth;

import co.za.rockmission.apparelapi.auth.dto.AuthResponse;
import co.za.rockmission.apparelapi.auth.dto.ForgotPasswordRequest;
import co.za.rockmission.apparelapi.auth.dto.ForgotPasswordResponse;
import co.za.rockmission.apparelapi.auth.dto.ForgotUsernameRequest;
import co.za.rockmission.apparelapi.auth.dto.LoginRequest;
import co.za.rockmission.apparelapi.auth.dto.RegisterRequest;
import co.za.rockmission.apparelapi.auth.dto.ResetPasswordRequest;
import co.za.rockmission.apparelapi.auth.dto.UpdateProfileRequest;
import co.za.rockmission.apparelapi.auth.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successful. Please sign in."));
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<Map<String, String>> forgotUsername(@Valid @RequestBody ForgotUsernameRequest request) {
        authService.forgotUsername(request);
        return ResponseEntity.ok(Map.of(
                "message",
                "If an account exists for this email, sign-in details have been prepared."
        ));
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return authService.me(authorizationHeader);
    }

    @PutMapping("/me")
    public UserResponse updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(authorizationHeader, request);
    }
}
