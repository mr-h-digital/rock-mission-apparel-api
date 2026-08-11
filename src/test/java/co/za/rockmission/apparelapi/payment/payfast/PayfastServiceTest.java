package co.za.rockmission.apparelapi.payment.payfast;

import static org.assertj.core.api.Assertions.assertThat;

import co.za.rockmission.apparelapi.order.Order;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PayfastServiceTest {

    private final PayfastProperties properties =
            new PayfastProperties("10000100", "46f0cd694581a", "", true,
                    "http://localhost:5173/order/success",
                    "http://localhost:5173/order/cancel",
                    "http://localhost:8080/api/payfast/notify");

    private final PayfastService service = new PayfastService(properties);

    @Test
    void buildPaymentFields_includesAValidSignature() {
        Order order = new Order();
        order.setFirstName("Lee");
        order.setLastName("H");
        order.setEmail("lee@example.com");
        order.setTotalAmount(new BigDecimal("649.00"));

        Map<String, String> fields = service.buildPaymentFields(order);

        assertThat(fields).containsKeys("merchant_id", "merchant_key", "m_payment_id", "amount", "signature");
        assertThat(fields.get("amount")).isEqualTo("649.00");
        assertThat(fields.get("signature")).hasSize(32); // MD5 hex digest length
    }

    @Test
    void verifyItnSignature_roundTripsAgainstOurOwnGeneratedSignature() {
        // Simulates verifying a notification whose fields+signature were produced the same way
        // PayFast documents for ITN: url-encoded key=value pairs joined with '&', MD5 hashed.
        String body = "m_payment_id=abc-123&pf_payment_id=999&payment_status=COMPLETE"
                + "&amount_gross=649.00&name_first=Lee";
        String signature = md5(body);
        String fullBody = body + "&signature=" + signature;

        assertThat(service.verifyItnSignature(fullBody, signature)).isTrue();
    }

    @Test
    void verifyItnSignature_rejectsATamperedAmount() {
        String original = "m_payment_id=abc-123&payment_status=COMPLETE&amount_gross=649.00";
        String signature = md5(original);
        String tampered = "m_payment_id=abc-123&payment_status=COMPLETE&amount_gross=1.00&signature=" + signature;

        assertThat(service.verifyItnSignature(tampered, signature)).isFalse();
    }

    @Test
    void parseFormBody_preservesOrderAndDecodesValues() {
        Map<String, String> parsed = service.parseFormBody("name_first=John%20Doe&amount=100.00");

        assertThat(parsed).containsExactly(
                Map.entry("name_first", "John Doe"),
                Map.entry("amount", "100.00"));
    }

    private String md5(String input) {
        try {
            var digest = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
