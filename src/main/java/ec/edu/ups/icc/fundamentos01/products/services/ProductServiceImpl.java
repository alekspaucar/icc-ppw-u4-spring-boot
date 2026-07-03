package ec.edu.ups.icc.fundamentos01.products.services;

import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.*;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll().stream()
                .filter(e -> e.isDeleted() == false)
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponseDto findOne(Long id) {
        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return ProductMapper.toResponse(entity);
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        UserEntity owner = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (owner.isDeleted()) throw new NotFoundException("User not found");
        if (productRepository.findByNameIgnoreCaseAndDeletedFalse(dto.getName()).isPresent())
            throw new ConflictException("Product name already registered");
        Set<CategoryEntity> categories = validateAndGetCategories(dto.getCategoryIds());
        ProductEntity entity = new ProductEntity();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setOwner(owner);
        entity.setCategories(categories);
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {
        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        Set<CategoryEntity> categories = validateAndGetCategories(dto.getCategoryIds());
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setCategories(categories);
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Override
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto) {
        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getStock() != null) entity.setStock(dto.getStock());
        if (dto.getCategoryIds() != null && dto.getCategoryIds().isEmpty() == false) {
            entity.setCategories(validateAndGetCategories(dto.getCategoryIds()));
        }
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        entity.setDeleted(true);
        productRepository.save(entity);
    }

    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {
        if (userRepository.existsByIdAndDeletedFalse(userId) == false)
            throw new NotFoundException("User not found");
        return productRepository.findByOwner_IdAndDeletedFalse(userId)
                .stream().map(ProductMapper::toResponse).toList();
    }

    @Override
    public List<ProductResponseDto> findByUserIdWithFilters(Long userId, ProductFilterByUserDto filters) {
        if (userRepository.existsByIdAndDeletedFalse(userId) == false)
            throw new NotFoundException("User not found");
        validateUserFilters(filters);
        String name = normalizeName(filters.getName());
        return productRepository.findByOwnerIdWithFilters(userId, name, filters.getMinPrice(), filters.getMaxPrice())
                .stream().map(ProductMapper::toResponse).toList();
    }

    @Override
    public List<ProductResponseDto> findByCategoryIdWithFilters(Long categoryId, ProductFilterByCategoryDto filters) {
        if (categoryRepository.existsByIdAndDeletedFalse(categoryId) == false)
            throw new NotFoundException("Category not found");
        validateCategoryFilters(filters);
        if (filters.getUserId() != null && userRepository.existsByIdAndDeletedFalse(filters.getUserId()) == false)
            throw new NotFoundException("User not found");
        String name = normalizeName(filters.getName());
        return productRepository.findByCategoryIdWithFilters(categoryId, name, filters.getMinPrice(), filters.getMaxPrice(), filters.getUserId())
                .stream().map(ProductMapper::toResponse).toList();
    }

    private Set<CategoryEntity> validateAndGetCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty())
            throw new BadRequestException("Debe seleccionar al menos una categoria");
        Set<CategoryEntity> categories = new HashSet<>();
        for (Long catId : categoryIds) {
            CategoryEntity category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            if (category.isDeleted()) throw new NotFoundException("Category not found");
            categories.add(category);
        }
        return categories;
    }

    private void validateUserFilters(ProductFilterByUserDto filters) {
        if (filters != null && filters.hasValidPriceRange() == false)
            throw new BadRequestException("El precio maximo debe ser mayor o igual al precio minimo");
    }

    private void validateCategoryFilters(ProductFilterByCategoryDto filters) {
        if (filters != null && filters.hasValidPriceRange() == false)
            throw new BadRequestException("El precio maximo debe ser mayor o igual al precio minimo");
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.trim();
    }
}