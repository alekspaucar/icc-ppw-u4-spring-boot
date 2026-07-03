package ec.edu.ups.icc.fundamentos01.users.controllers;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserProductsController {

    private final ProductService productService;

    public UserProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}/products")
    public List<ProductResponseDto> findProductsByUser(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductFilterByUserDto filters
    ) {
        return productService.findByUserIdWithFilters(id, filters);
    }
}