package co.za.rockmission.apparelapi.product;

import co.za.rockmission.apparelapi.common.UnauthorizedException;
import co.za.rockmission.apparelapi.storage.StorageService;
import co.za.rockmission.apparelapi.auth.AppUser;
import co.za.rockmission.apparelapi.auth.AppUserRepository;
import co.za.rockmission.apparelapi.auth.JwtService;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class AdminMediaController {

    private final StorageService storageService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Value("${app.admin-emails:}")
    private String adminEmailsRaw;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadImage(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(authorizationHeader);
        StorageService.UploadedFile uploadedFile = storageService.uploadProductImage(file);
        return Map.of("url", uploadedFile.url(), "key", uploadedFile.key());
    }

    private void requireAdmin(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        UUID userId = jwtService.parseUserId(token);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists."));

        String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase(Locale.ROOT);
        boolean allowed = Arrays.stream(adminEmailsRaw.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .map(v -> v.toLowerCase(Locale.ROOT))
                .anyMatch(v -> v.equals(email));

        if (!allowed) {
            throw new UnauthorizedException("Admin access required.");
        }
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
}