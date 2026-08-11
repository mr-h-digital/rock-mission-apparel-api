package co.za.rockmission.apparelapi.payment.payfast;

import co.za.rockmission.apparelapi.fulfillment.PrintfulService;
import co.za.rockmission.apparelapi.order.Order;
import co.za.rockmission.apparelapi.order.OrderRepository;
import co.za.rockmission.apparelapi.order.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * Receives PayFast's Instant Transaction Notification (ITN) webhook after a customer completes
 * (or cancels) payment. Never trust this endpoint's data without: signature verification, the
 * server-to-server "validate" round-trip back to PayFast, and an amount/order check - all three
 * are required by PayFast's own integration guidelines to prevent a forged notification from
 * marking an order paid.
 */
@RestController
@RequestMapping("/api/payfast")
@RequiredArgsConstructor
public class PayfastController {

    private static final Logger log = LoggerFactory.getLogger(PayfastController.class);

    private final PayfastService payfastService;
    private final PayfastProperties properties;
    private final OrderRepository orderRepository;
    private final PrintfulService printfulService;
    private final RestClient.Builder restClientBuilder;

    @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> notify(HttpServletRequest request) throws IOException {
        String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = payfastService.parseFormBody(rawBody);
        String receivedSignature = params.get("signature");

        if (receivedSignature == null || !payfastService.verifyItnSignature(rawBody, receivedSignature)) {
            log.warn("PayFast ITN signature mismatch, ignoring notification");
            return ResponseEntity.badRequest().build();
        }

        if (!validateWithPayfast(rawBody)) {
            log.warn("PayFast ITN failed server-to-server validation, ignoring notification");
            return ResponseEntity.badRequest().build();
        }

        UUID orderId;
        try {
            orderId = UUID.fromString(params.get("m_payment_id"));
        } catch (Exception e) {
            log.warn("PayFast ITN had an unrecognisable m_payment_id: {}", params.get("m_payment_id"));
            return ResponseEntity.badRequest().build();
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("PayFast ITN referenced unknown order {}", orderId);
            return ResponseEntity.badRequest().build();
        }

        if (order.getStatus() == OrderStatus.PAID) {
            // Already processed - PayFast may retry notifications, so this must be idempotent.
            return ResponseEntity.ok().build();
        }

        String paymentStatus = params.get("payment_status");
        BigDecimal grossAmount = parseAmount(params.get("amount_gross"));

        if (!"COMPLETE".equals(paymentStatus)) {
            order.setStatus(OrderStatus.FAILED);
            order.setPayfastPaymentId(params.get("pf_payment_id"));
            order.touch();
            orderRepository.save(order);
            return ResponseEntity.ok().build();
        }

        if (grossAmount == null || grossAmount.compareTo(order.getTotalAmount()) != 0) {
            log.warn(
                    "PayFast ITN amount mismatch for order {}: expected {} got {}",
                    orderId,
                    order.getTotalAmount(),
                    grossAmount);
            return ResponseEntity.badRequest().build();
        }

        order.setStatus(OrderStatus.PAID);
        order.setPayfastPaymentId(params.get("pf_payment_id"));
        order.touch();
        orderRepository.save(order);

        printfulService.submitOrder(order);

        return ResponseEntity.ok().build();
    }

    private boolean validateWithPayfast(String rawBody) {
        try {
            ResponseEntity<String> response = restClientBuilder
                    .build()
                    .post()
                    .uri(properties.validateUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(rawBody)
                    .retrieve()
                    .toEntity(String.class);
            String body = response.getBody();
            return body != null && body.trim().equalsIgnoreCase("VALID");
        } catch (Exception e) {
            log.error("PayFast ITN server-to-server validation call failed", e);
            return false;
        }
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
