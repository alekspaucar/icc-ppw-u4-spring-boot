package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.time.LocalDateTime;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;


public class ProductResponseDto {

    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private UserResponseDto owner;

    private CategoryResponseDto category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor vacío
    public ProductResponseDto() {}


    // Constructor lleno
    public ProductResponseDto(Long id, String name, Double price, Integer stock, UserResponseDto owner, CategoryResponseDto category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.owner = owner;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    // Getters y setters

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public UserResponseDto getOwner() {
        return owner;
    }

    public void setOwner(UserResponseDto owner) {
        this.owner = owner;
    }

    public CategoryResponseDto getCategory() {
        return category;
    }

    public void setCategory(CategoryResponseDto category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
}