package co.za.rockmission.apparelapi.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByActiveTrue();
}
