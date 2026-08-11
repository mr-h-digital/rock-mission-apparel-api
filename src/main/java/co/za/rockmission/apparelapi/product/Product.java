package co.za.rockmission.apparelapi.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product {

    @Id
    private String id;

    private String name;

    private String category;

    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;
}
