package co.za.rockmission.apparelapi.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public List<ProductDto> list() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream().map(ProductDto::from).toList();
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable String id) {
        return productRepository
                .findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new co.za.rockmission.apparelapi.common.NotFoundException("Product not found: " + id));
    }
}
