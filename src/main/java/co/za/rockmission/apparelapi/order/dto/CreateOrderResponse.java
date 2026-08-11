package co.za.rockmission.apparelapi.order.dto;

import java.util.Map;
import java.util.UUID;

public record CreateOrderResponse(UUID orderId, String processUrl, Map<String, String> fields) {
}
