package co.za.rockmission.apparelapi.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "size", "color"}))
@Getter
@Setter
@NoArgsConstructor
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 32)
    private String size;

    @Column(nullable = false, length = 64)
    private String color;

    @Column(name = "stock_on_hand", nullable = false)
    private int stockOnHand;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    public int getAvailableQuantity() {
        return stockOnHand - reservedQuantity;
    }
}
