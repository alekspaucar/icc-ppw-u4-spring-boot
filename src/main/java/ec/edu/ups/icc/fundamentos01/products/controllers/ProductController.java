package ec.edu.ups.icc.fundamentos01.products.controllers;
import ec.edu.ups.icc.fundamentos01.products.dtos.*;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @GetMapping public List<ProductResponseDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public ProductResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }
    @PostMapping public ProductResponseDto create(@Valid @RequestBody CreateProductDto dto) { return service.create(dto); }
    @PutMapping("/{id}") public ProductResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateProductDto dto) { return service.update(id, dto); }
    @PatchMapping("/{id}") public ProductResponseDto partialUpdate(@PathVariable Long id, @Valid @RequestBody PartialUpdateProductDto dto) { return service.partialUpdate(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}