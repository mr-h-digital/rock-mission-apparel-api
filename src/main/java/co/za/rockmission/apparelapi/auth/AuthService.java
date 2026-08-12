package co.za.rockmission.apparelapi.auth;

import co.za.rockmission.apparelapi.auth.dto.AuthResponse;
import co.za.rockmission.apparelapi.auth.dto.ForgotPasswordRequest;
import co.za.rockmission.apparelapi.auth.dto.ForgotPasswordResponse;
import co.za.rockmission.apparelapi.auth.dto.ForgotUsernameRequest;
import co.za.rockmission.apparelapi.auth.dto.LoginRequest;
import co.za.rockmission.apparelapi.auth.dto.RegisterRequest;
import co.za.rockmission.apparelapi.auth.dto.ResetPasswordRequest;
import co.za.rockmission.apparelapi.auth.dto.UserResponse;
import co.za.rockmission.apparelapi.common.BadRequestException;
import co.za.rockmission.apparelapi.common.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.reset-token-minutes:30}")
    private long resetTokenMinutes;

    @Value("${app.auth.expose-reset-token:false}")
    private boolean exposeResetToken;

    @Value("${app.auth.reset-base-url:https://shop.rockmission.co.za/reset-password}")
    private String resetBaseUrl;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        appUserRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new BadRequestException("An account with this email already exists.");
        });

        AppUser user = new AppUser();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        AppUser saved = appUserRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(token, UserResponse.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.from(user));
    }

    public UserResponse me(String bearerToken) {
        UUID userId = jwtService.parseUserId(extractBearerToken(bearerToken));

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists."));

        return UserResponse.from(user);
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());

        String exposedToken = null;
        String exposedResetUrl = null;

        AppUser user = appUserRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user != null) {
            revokeOutstandingResetTokens(user);

            String plainToken = generateResetToken();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(sha256(plainToken));
            resetToken.setExpiresAt(Instant.now().plusSeconds(resetTokenMinutes * 60));
            passwordResetTokenRepository.save(resetToken);

            if (exposeResetToken) {
                exposedToken = plainToken;
                exposedResetUrl = resetBaseUrl + "?token=" + plainToken;
            }
        }

        return new ForgotPasswordResponse(
                "If an account exists for this email, a reset link has been prepared.",
                exposedToken,
                exposedResetUrl
        );
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(sha256(request.token().trim()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token."));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token.");
        }

        AppUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.touch();
        appUserRepository.save(user);

        revokeOutstandingResetTokens(user);
    }

    public void forgotUsername(ForgotUsernameRequest request) {
        // Deliberately generic to avoid account enumeration. Sign-in ID is email.
        normalizeEmail(request.email());
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header.");
        }

        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix) || authorizationHeader.length() <= prefix.length()) {
            throw new UnauthorizedException("Authorization header must be in the form: Bearer <token>.");
        }

        return authorizationHeader.substring(prefix.length()).trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void revokeOutstandingResetTokens(AppUser user) {
        Instant now = Instant.now();
        var tokens = passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(user.getId());
        tokens.forEach(token -> token.setUsedAt(now));
        passwordResetTokenRepository.saveAll(tokens);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash reset token.", ex);
        }
    }
}
