package co.za.rockmission.apparelapi.product;

import co.za.rockmission.apparelapi.common.BadRequestException;
import co.za.rockmission.apparelapi.order.Order;
import co.za.rockmission.apparelapi.order.OrderItem;
import co.za.rockmission.apparelapi.order.OrderRepository;
import co.za.rockmission.apparelapi.order.OrderStatus;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductInventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void reserve(Order order) {
        Set<String> trackedProducts = new HashSet<>();
        order.getItems().forEach(item -> {
            if (inventoryRepository.findByProductIdOrderBySizeAscColorAsc(item.getProductId()).isEmpty()) return;
            trackedProducts.add(item.getProductId());
            ProductInventory inventory = lockVariant(item);
            int available = inventory.getAvailableQuantity();
            if (item.getQty() > available) {
                throw new BadRequestException("Not enough stock for " + item.getProductName()
                        + " (" + item.getSize() + "/" + item.getColor() + "). Available: " + available);
            }
            inventory.setReservedQuantity(inventory.getReservedQuantity() + item.getQty());
        });
    }

    @Transactional
    public void release(Order order) {
        order.getItems().forEach(item -> {
            ProductInventory inventory = lockVariantIfTracked(item);
            if (inventory == null) return;
            inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - item.getQty()));
        });
    }

    @Transactional
    public void commit(Order order) {
        order.getItems().forEach(item -> {
            ProductInventory inventory = lockVariantIfTracked(item);
            if (inventory == null) return;
            if (inventory.getReservedQuantity() < item.getQty()) {
                throw new IllegalStateException("Reserved stock is inconsistent for order " + order.getId());
            }
            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQty());
            inventory.setStockOnHand(inventory.getStockOnHand() - item.getQty());
        });
    }

    @Transactional
    public void restore(Order order) {
        order.getItems().forEach(item -> {
            ProductInventory inventory = lockVariantIfTracked(item);
            if (inventory == null) return;
            inventory.setStockOnHand(inventory.getStockOnHand() + item.getQty());
        });
    }

    @Scheduled(fixedDelay = 900000, initialDelay = 900000)
    @Transactional
    public void expireStaleReservations() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.MINUTES);
        orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff).forEach(order -> {
            release(order);
            order.setStatus(OrderStatus.FAILED);
            order.touch();
            orderRepository.save(order);
        });
    }

    private ProductInventory lockVariant(OrderItem item) {
        return inventoryRepository.findForUpdate(item.getProductId(), item.getSize(), item.getColor())
                .orElseThrow(() -> new BadRequestException("This product variant is not available: "
                        + item.getProductName() + " (" + item.getSize() + "/" + item.getColor() + ")."));
    }

    private ProductInventory lockVariantIfTracked(OrderItem item) {
        if (inventoryRepository.findByProductIdOrderBySizeAscColorAsc(item.getProductId()).isEmpty()) return null;
        return lockVariant(item);
    }
}
