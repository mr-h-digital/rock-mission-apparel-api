package co.za.rockmission.apparelapi.payment.payfast;

import co.za.rockmission.apparelapi.order.Order;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Builds outgoing PayFast payment requests and verifies incoming ITN
 * (Instant Transaction Notification) webhooks.
 *
 * @see <a href="https://developers.payfast.co.za/docs">PayFast developer docs</a>
 */
@Service
@RequiredArgsConstructor
public class PayfastService {

    private final PayfastProperties properties;

    /** Builds the field set for the hidden form the frontend POSTs to PayFast's process URL. */
    public Map<String, String> buildPaymentFields(Order order) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant_id", properties.merchantId());
        fields.put("merchant_key", properties.merchantKey());
        fields.put("return_url", properties.returnUrl());
        fields.put("cancel_url", properties.cancelUrl());
        fields.put("notify_url", properties.notifyUrl());
        fields.put("name_first", order.getFirstName());
        fields.put("name_last", order.getLastName());
        fields.put("email_address", order.getEmail());
        fields.put("m_payment_id", order.getId().toString());
        fields.put("amount", formatAmount(order.getTotalAmount()));
        fields.put("item_name", "Kingdom Drip Order");

        String signature = signature(fields, properties.passphrase());
        fields.put("signature", signature);
        return fields;
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    /** Signature for an outgoing payment request: our own fields, in insertion order. */
    private String signature(Map<String, String> fields, String passphrase) {
        StringBuilder sb = new StringBuilder();
        fields.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                appendParam(sb, key, value);
            }
        });
        if (passphrase != null && !passphrase.isBlank()) {
            appendParam(sb, "passphrase", passphrase);
        }
        return md5Hex(trimTrailingAmpersand(sb));
    }

    /**
     * Verifies an ITN signature using the raw, still-encoded POST body PayFast sent - the
     * signature must be recomputed over the exact bytes received (same order, same encoding),
     * not over a re-encoded copy, or it will never match.
     */
    public boolean verifyItnSignature(String rawBody, String receivedSignature) {
        String withoutSignature = rawBody.replaceFirst("&?signature=[^&]*", "");
        String passphrase = properties.passphrase();
        String toHash = withoutSignature;
        if (passphrase != null && !passphrase.isBlank()) {
            String separator = toHash.isEmpty() || toHash.endsWith("&") ? "" : "&";
            toHash = toHash + separator + "passphrase=" + urlEncode(passphrase);
        }
        String computed = md5Hex(toHash);
        return computed.equalsIgnoreCase(receivedSignature);
    }

    /** Parses a form-urlencoded body into an order-preserving, URL-decoded map. */
    public Map<String, String> parseFormBody(String rawBody) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (rawBody == null || rawBody.isBlank()) {
            return result;
        }
        for (String pair : rawBody.split("&")) {
            if (pair.isBlank()) continue;
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String value = idx >= 0 ? pair.substring(idx + 1) : "";
            result.put(urlDecode(key), urlDecode(value));
        }
        return result;
    }

    private void appendParam(StringBuilder sb, String key, String value) {
        sb.append(key).append('=').append(urlEncode(value)).append('&');
    }

    private String trimTrailingAmpersand(StringBuilder sb) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String urlDecode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String md5Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
