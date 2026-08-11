package co.za.rockmission.apparelapi.order;

import co.za.rockmission.apparelapi.common.BadRequestException;
import co.za.rockmission.apparelapi.order.dto.CreateOrderRequest;
import co.za.rockmission.apparelapi.order.dto.CreateOrderResponse;
import co.za.rockmission.apparelapi.order.dto.OrderItemRequest;
import co.za.rockmission.apparelapi.payment.payfast.PayfastProperties;
import co.za.rockmission.apparelapi.payment.payfast.PayfastService;
import co.za.rockmission.apparelapi.product.Product;
import co.za.rockmission.apparelapi.product.ProductRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PayfastService payfastService;
    private final PayfastProperties payfastProperties;

    @PostMapping
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setFirstName(request.customer().firstName());
        order.setLastName(request.customer().lastName());
        order.setEmail(request.customer().email());
        order.setPhone(request.customer().phone());
        order.setAddressLine1(request.customer().addressLine1());
        order.setAddressLine2(request.customer().addressLine2());
        order.setCity(request.customer().city());
        order.setProvince(request.customer().province());
        order.setPostalCode(request.customer().postalCode());
        order.setCountry(request.customer().country());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository
                    .findById(itemRequest.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new BadRequestException("Unknown product: " + itemRequest.productId()));

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSize(itemRequest.size());
            item.setColor(itemRequest.color());
            item.setQty(itemRequest.qty());
            // Price is always taken from the server-side catalog, never the client - the
            // frontend cart total is for display only, so a tampered request can't undercharge.
            item.setUnitPrice(product.getPrice());
            order.addItem(item);

            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);

        orderRepository.save(order);

        var fields = payfastService.buildPaymentFields(order);
        return new CreateOrderResponse(order.getId(), payfastProperties.processUrl(), fields);
    }
}
