package co.za.rockmission.apparelapi.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByPayfastPaymentId(String payfastPaymentId);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant cutoff);
}
