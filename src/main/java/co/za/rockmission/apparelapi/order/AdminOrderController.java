package co.za.rockmission.apparelapi.order;

import co.za.rockmission.apparelapi.auth.AppUser;
import co.za.rockmission.apparelapi.auth.AppUserRepository;
import co.za.rockmission.apparelapi.auth.JwtService;
import co.za.rockmission.apparelapi.common.NotFoundException;
import co.za.rockmission.apparelapi.common.UnauthorizedException;
import co.za.rockmission.apparelapi.order.dto.AdminOrderDto;
import co.za.rockmission.apparelapi.product.InventoryService;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Value("${app.admin-emails:}")
    private String adminEmailsRaw;

    @GetMapping
    public List<AdminOrderDto> list(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(AdminOrderDto::from).toList();
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    public AdminOrderDto cancel(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID id) {
        requireAdmin(authorizationHeader);
        Order order = orderRepository.findForUpdateById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        if (order.getStatus() == OrderStatus.PAID) {
            inventoryService.restore(order);
        } else if (order.getStatus() == OrderStatus.PENDING) {
            inventoryService.release(order);
        }

        if (order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            order.touch();
            orderRepository.save(order);
        }
        return AdminOrderDto.from(order);
    }

    private void requireAdmin(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        UUID userId = jwtService.parseUserId(token);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists."));
        String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase(Locale.ROOT);
        boolean allowed = Arrays.stream(adminEmailsRaw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(email));
        if (!allowed) throw new UnauthorizedException("Admin access required.");
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
