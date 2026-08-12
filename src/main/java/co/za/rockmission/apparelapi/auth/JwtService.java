package co.za.rockmission.apparelapi.auth;

import co.za.rockmission.apparelapi.common.UnauthorizedException;
import co.za.rockmission.apparelapi.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expiryHours;

    public JwtService(AppProperties appProperties) {
        String secret = appProperties.jwtSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("AUTH_JWT_SECRET must be at least 32 characters.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryHours = appProperties.jwtExpiryHours();
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(Math.max(1, expiryHours) * 3600);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public UUID parseUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid or expired token.");
        }
    }
}
