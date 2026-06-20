package ec.edu.ups.icc.fundamentos01.products.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public List<ProductResponseDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ProductResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    public ProductResponseDto create(@RequestBody CreateProductDto dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public ProductResponseDto update(@PathVariable Long id, @RequestBody UpdateProductDto dto) { return service.update(id, dto); }

    @PatchMapping("/{id}")
    public ProductResponseDto partialUpdate(@PathVariable Long id, @RequestBody PartialUpdateProductDto dto) { return service.partialUpdate(id, dto); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
