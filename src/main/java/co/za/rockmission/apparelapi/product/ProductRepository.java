package co.za.rockmission.apparelapi.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    @EntityGraph(attributePaths = "inventory")
    List<Product> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "inventory")
    List<Product> findAllByOrderByNameAsc();

    @Override
    @EntityGraph(attributePaths = "inventory")
    Optional<Product> findById(String id);
}
