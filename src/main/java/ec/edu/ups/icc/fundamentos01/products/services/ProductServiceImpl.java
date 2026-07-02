package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll()
                .stream()
                .filter(entity -> !entity.isDeleted())
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponseDto findOne(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return ProductMapper.toResponse(entity);
    }

    /*
     * Crea un producto asociado a un usuario y a una categoría.
     *
     * Valida:
     * - que el usuario exista
     * - que la categoría exista
     * - que no exista un producto activo con el mismo nombre
     */
    @Override
    public ProductResponseDto create(CreateProductDto dto) {

        UserEntity owner = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (owner.isDeleted()) {
            throw new NotFoundException("User not found");
        }

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (category.isDeleted()) {
            throw new NotFoundException("Category not found");
        }

        if (productRepository.findByNameIgnoreCaseAndDeletedFalse(dto.getName()).isPresent()) {
            throw new ConflictException("Product name already registered");
        }

        ProductEntity entity = new ProductEntity();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setOwner(owner);
        entity.setCategory(category);

        ProductEntity savedEntity = productRepository.save(entity);

        return ProductMapper.toResponse(savedEntity);
    }

    /*
     * Actualiza completamente un producto activo.
     * No permite cambiar el usuario propietario.
     * Sí permite cambiar la categoría.
     */
    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {

        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (category.isDeleted()) {
            throw new NotFoundException("Category not found");
        }

        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setCategory(category);

        ProductEntity savedEntity = productRepository.save(entity);

        return ProductMapper.toResponse(savedEntity);
    }

    /*
     * Actualiza parcialmente un producto activo.
     * Solo modifica los campos enviados en el DTO.
     */
    @Override
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto) {

        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }

        if (dto.getStock() != null) {
            entity.setStock(dto.getStock());
        }

        if (dto.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));

            if (category.isDeleted()) {
                throw new NotFoundException("Category not found");
            }

            entity.setCategory(category);
        }

        ProductEntity savedEntity = productRepository.save(entity);

        return ProductMapper.toResponse(savedEntity);
    }

    @Override
    public void delete(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .filter(product -> !product.isDeleted())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        entity.setDeleted(true);
        productRepository.save(entity);
    }

    /*
     * Retorna los productos activos creados por un usuario.
     * Primero valida que el usuario exista y no esté eliminado.
     */
    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {
        if (!userRepository.existsByIdAndDeletedFalse(userId)) {
            throw new NotFoundException("User not found");
        }

        return productRepository.findByOwner_IdAndDeletedFalse(userId)
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    /*
     * Retorna los productos activos asociados a una categoría.
     * Primero valida que la categoría exista y no esté eliminada.
     */
    @Override
    public List<ProductResponseDto> findByCategoryId(Long categoryId) {
        if (!categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        return productRepository.findByCategory_IdAndDeletedFalse(categoryId)
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }
}