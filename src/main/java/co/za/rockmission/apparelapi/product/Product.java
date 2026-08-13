package co.za.rockmission.apparelapi.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    private String id;

    private String name;

    private String category;

    private BigDecimal price;

    private String imageUrl;

    private String blurb;

    private String art;

    private String word;

    private String sizesCsv;

    private String colorsCsv;

    @Column(nullable = false)
    private boolean active = true;

    @jakarta.persistence.OneToMany(mappedBy = "product", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ProductInventory> inventory = new ArrayList<>();
}
