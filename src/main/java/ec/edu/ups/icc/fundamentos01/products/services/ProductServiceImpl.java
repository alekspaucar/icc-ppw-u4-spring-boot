package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByCategoryDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository,
            CategoryRepository categoryRepository) {
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
        return ProductMapper.toResponse(findActiveProductOrThrow(id));
    }

    @Override
    @Transactional
    public ProductResponseDto create(CreateProductDto dto, UserDetailsImpl currentUser) {
        UserEntity owner = findCurrentUserEntity(currentUser);
        validateProductNameForCreate(dto.getName());
        Set<CategoryEntity> categories = findActiveCategories(dto.getCategoryIds());
        ProductEntity entity = new ProductEntity();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setOwner(owner);
        entity.setCategories(categories);
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductResponseDto update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser) {
        ProductEntity entity = findActiveProductOrThrow(id);
        validateOwnership(entity, currentUser);
        validateProductNameForUpdate(id, dto.getName());
        Set<CategoryEntity> categories = findActiveCategories(dto.getCategoryIds());
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setCategories(categories);
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto, UserDetailsImpl currentUser) {
        ProductEntity entity = findActiveProductOrThrow(id);
        validateOwnership(entity, currentUser);
        if (dto.getName() != null) {
            validateProductNameForUpdate(id, dto.getName());
            entity.setName(dto.getName());
        }
        if (dto.getPrice() != null)
            entity.setPrice(dto.getPrice());
        if (dto.getStock() != null)
            entity.setStock(dto.getStock());
        if (dto.getCategoryIds() != null && dto.getCategoryIds().isEmpty() == false) {
            Set<CategoryEntity> categories = findActiveCategories(dto.getCategoryIds());
            entity.setCategories(categories);
        }
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id, UserDetailsImpl currentUser) {
        ProductEntity entity = findActiveProductOrThrow(id);
        validateOwnership(entity, currentUser);
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
        return productRepository
                .findByCategoryIdWithFilters(categoryId, name, filters.getMinPrice(), filters.getMaxPrice(),
                        filters.getUserId())
                .stream().map(ProductMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllPage(PaginationDto pagination) {
        return productRepository.findActivePage(createPageable(pagination)).map(ProductMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ProductResponseDto> findAllSlice(PaginationDto pagination) {
        return productRepository.findActiveSlice(createPageable(pagination)).map(ProductMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findByCategoryIdWithFiltersPage(Long categoryId, ProductFilterByCategoryDto filters,
            PaginationDto pagination) {
        if (categoryRepository.existsByIdAndDeletedFalse(categoryId) == false)
            throw new NotFoundException("Category not found");
        validateCategoryFilters(filters);
        String name = normalizeName(filters.getName());
        return productRepository
                .findByCategoryIdWithFiltersPage(categoryId, name, filters.getMinPrice(), filters.getMaxPrice(),
                        filters.getUserId(), createPageable(pagination))
                .map(ProductMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ProductResponseDto> findByCategoryIdWithFiltersSlice(Long categoryId,
            ProductFilterByCategoryDto filters, PaginationDto pagination) {
        if (categoryRepository.existsByIdAndDeletedFalse(categoryId) == false)
            throw new NotFoundException("Category not found");
        validateCategoryFilters(filters);
        String name = normalizeName(filters.getName());
        return productRepository
                .findByCategoryIdWithFiltersSlice(categoryId, name, filters.getMinPrice(), filters.getMaxPrice(),
                        filters.getUserId(), createPageable(pagination))
                .map(ProductMapper::toResponse);
    }


    private ProductEntity findActiveProductOrThrow(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private UserEntity findCurrentUserEntity(UserDetailsImpl currentUser) {
        if (currentUser == null)
            throw new AccessDeniedException("Usuario no autenticado");
        return userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado"));
    }

    private void validateOwnership(ProductEntity product, UserDetailsImpl currentUser) {
        if (currentUser == null)
            throw new AccessDeniedException("Usuario no autenticado");
        if (hasRole(currentUser, "ROLE_ADMIN"))
            return;
        if (product.getOwner() == null || product.getOwner().getId() == null)
            throw new AccessDeniedException("El producto no tiene propietario valido");
        if (product.getOwner().getId().equals(currentUser.getId()) == false)
            throw new AccessDeniedException("No puedes modificar productos ajenos");
    }

    private boolean hasRole(UserDetailsImpl user, String role) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }

    private void validateProductNameForCreate(String name) {
        if (productRepository.findByNameIgnoreCaseAndDeletedFalse(name.trim()).isPresent())
            throw new ConflictException("Product name already registered");
    }

    private void validateProductNameForUpdate(Long currentProductId, String name) {
        productRepository.findByNameIgnoreCaseAndDeletedFalse(name.trim())
                .filter(product -> product.getId().equals(currentProductId) == false)
                .ifPresent(product -> {
                    throw new ConflictException("Product name already registered");
                });
    }

    private Set<CategoryEntity> findActiveCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty())
            throw new BadRequestException("Debe seleccionar al menos una categoria");
        Set<Long> uniqueIds = new HashSet<>(categoryIds);
        Set<CategoryEntity> categories = categoryRepository.findAllById(uniqueIds).stream()
                .filter(c -> c.isDeleted() == false)
                .collect(Collectors.toSet());
        if (categories.size() != uniqueIds.size())
            throw new NotFoundException("One or more categories were not found");
        return categories;
    }

    private Pageable createPageable(PaginationDto pagination) {
        String sortBy = normalizeSortBy(pagination.getSortBy());
        Sort.Direction direction = normalizeDirection(pagination.getDirection());
        return PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by(direction, sortBy));
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank())
            return "id";
        Set<String> allowed = Set.of("id", "name", "price", "stock", "createdAt", "updatedAt");
        if (allowed.contains(sortBy) == false)
            throw new BadRequestException("Campo de ordenamiento no permitido: " + sortBy);
        return sortBy;
    }

    private Sort.Direction normalizeDirection(String direction) {
        if (direction == null || direction.isBlank())
            return Sort.Direction.ASC;
        if (direction.equalsIgnoreCase("asc"))
            return Sort.Direction.ASC;
        if (direction.equalsIgnoreCase("desc"))
            return Sort.Direction.DESC;
        throw new BadRequestException("Direccion de ordenamiento no valida: " + direction);
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
        if (name == null || name.isBlank())
            return null;
        return name.trim();
    }
}