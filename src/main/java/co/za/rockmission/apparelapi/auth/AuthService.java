package co.za.rockmission.apparelapi.auth;

import co.za.rockmission.apparelapi.auth.dto.AuthResponse;
import co.za.rockmission.apparelapi.auth.dto.LoginRequest;
import co.za.rockmission.apparelapi.auth.dto.RegisterRequest;
import co.za.rockmission.apparelapi.auth.dto.UserResponse;
import co.za.rockmission.apparelapi.common.BadRequestException;
import co.za.rockmission.apparelapi.common.UnauthorizedException;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
}
