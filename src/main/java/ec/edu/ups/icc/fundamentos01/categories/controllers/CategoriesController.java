package ec.edu.ups.icc.fundamentos01.categories.controllers;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.fundamentos01.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.fundamentos01.categories.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

    private final CategoryService service;

    public CategoriesController(CategoryService service) { this.service = service; }

    @GetMapping
    public List<CategoryResponseDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public CategoryResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    public CategoryResponseDto create(@Valid @RequestBody CreateCategoryDto dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public CategoryResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateCategoryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}