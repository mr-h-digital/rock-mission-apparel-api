package co.za.rockmission.apparelapi.product;

import co.za.rockmission.apparelapi.auth.AppUser;
import co.za.rockmission.apparelapi.auth.AppUserRepository;
import co.za.rockmission.apparelapi.auth.JwtService;
import co.za.rockmission.apparelapi.common.BadRequestException;
import co.za.rockmission.apparelapi.common.NotFoundException;
import co.za.rockmission.apparelapi.common.UnauthorizedException;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductRepository productRepository;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Value("${app.admin-emails:}")
    private String adminEmailsRaw;

    @GetMapping
    public List<ProductDto> listAll(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return productRepository.findAllByOrderByNameAsc().stream().map(ProductDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody ProductUpsertRequest request) {
        requireAdmin(authorizationHeader);

        if (productRepository.existsById(request.id().trim())) {
            throw new BadRequestException("Product id already exists: " + request.id());
        }

        Product product = new Product();
        applyRequest(product, request, request.id().trim());
        return ProductDto.from(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ProductDto update(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String id,
            @Valid @RequestBody ProductUpsertRequest request) {
        requireAdmin(authorizationHeader);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        String requestId = request.id().trim();
        if (!id.equals(requestId)) {
            throw new BadRequestException("Path id must match body id.");
        }

        applyRequest(product, request, id);
        return ProductDto.from(productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String id) {
        requireAdmin(authorizationHeader);

        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found: " + id);
        }

        productRepository.deleteById(id);
    }

    private void applyRequest(Product product, ProductUpsertRequest request, String id) {
        product.setId(id);
        product.setName(request.name().trim());
        product.setCategory(request.category().trim());
        product.setPrice(request.price());
        product.setImageUrl(normalizeOptional(request.imageUrl()));
        product.setBlurb(normalizeOptional(request.blurb()));
        product.setArt(normalizeOptional(request.art()));
        product.setWord(normalizeOptional(request.word()));
        product.setSizesCsv(listToCsv(request.sizes()));
        product.setColorsCsv(listToCsv(request.colors()));
        product.setActive(request.active());
    }

    private String listToCsv(List<String> values) {
        return values.stream().map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.joining(","));
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
