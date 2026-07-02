package ec.edu.ups.icc.fundamentos01.products.controllers;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import jakarta.validation.Valid;
@RestController @RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @GetMapping public List<ProductResponseDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public ProductResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }
    @GetMapping("/user/{userId}") public List<ProductResponseDto> findByUserId(@PathVariable Long userId) { return service.findByUserId(userId); }
    @GetMapping("/category/{categoryId}") public List<ProductResponseDto> findByCategoryId(@PathVariable Long categoryId) { return service.findByCategoryId(categoryId); }
    @PostMapping public ProductResponseDto create(@Valid @RequestBody CreateProductDto dto) { return service.create(dto); }
    @PutMapping("/{id}") public ProductResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateProductDto dto) { return service.update(id, dto); }
    @PatchMapping("/{id}") public ProductResponseDto partialUpdate(@PathVariable Long id, @Valid @RequestBody PartialUpdateProductDto dto) { return service.partialUpdate(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}