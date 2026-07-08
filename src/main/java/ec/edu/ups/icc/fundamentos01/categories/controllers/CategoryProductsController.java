package ec.edu.ups.icc.fundamentos01.categories.controllers;

import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByCategoryDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryProductsController {

    private final ProductService productService;

    public CategoryProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}/products")
    public List<ProductResponseDto> findProductsByCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductFilterByCategoryDto filters
    ) {
        return productService.findByCategoryIdWithFilters(id, filters);
    }

    @GetMapping("/{id}/products/page")
    public Page<ProductResponseDto> findProductsByCategoryPage(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductFilterByCategoryDto filters,
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return productService.findByCategoryIdWithFiltersPage(id, filters, pagination);
    }

    @GetMapping("/{id}/products/slice")
    public Slice<ProductResponseDto> findProductsByCategorySlice(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductFilterByCategoryDto filters,
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return productService.findByCategoryIdWithFiltersSlice(id, filters, pagination);
    }
}