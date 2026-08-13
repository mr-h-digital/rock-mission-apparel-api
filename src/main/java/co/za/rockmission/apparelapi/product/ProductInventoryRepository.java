package co.za.rockmission.apparelapi.product;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, UUID> {
    List<ProductInventory> findByProductIdOrderBySizeAscColorAsc(String productId);

    Optional<ProductInventory> findByProductIdAndSizeAndColor(String productId, String size, String color);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select inventory from ProductInventory inventory where inventory.product.id = :productId and inventory.size = :size and inventory.color = :color")
    Optional<ProductInventory> findForUpdate(String productId, String size, String color);
}
