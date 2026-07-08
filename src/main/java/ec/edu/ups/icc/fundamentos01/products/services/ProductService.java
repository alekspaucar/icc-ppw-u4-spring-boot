package ec.edu.ups.icc.fundamentos01.products.services;

import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import java.util.List;

public interface ProductService {
    List<ProductResponseDto> findAll();
    ProductResponseDto findOne(Long id);
    ProductResponseDto create(CreateProductDto dto);
    ProductResponseDto update(Long id, UpdateProductDto dto);
    ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto);
    void delete(Long id);
    List<ProductResponseDto> findByUserId(Long userId);
    List<ProductResponseDto> findByUserIdWithFilters(Long userId, ProductFilterByUserDto filters);
    List<ProductResponseDto> findByCategoryIdWithFilters(Long categoryId, ProductFilterByCategoryDto filters);
    Page<ProductResponseDto> findAllPage(PaginationDto pagination);
    Slice<ProductResponseDto> findAllSlice(PaginationDto pagination);
    Page<ProductResponseDto> findByCategoryIdWithFiltersPage(Long categoryId, ProductFilterByCategoryDto filters, PaginationDto pagination);
    Slice<ProductResponseDto> findByCategoryIdWithFiltersSlice(Long categoryId, ProductFilterByCategoryDto filters, PaginationDto pagination);
}