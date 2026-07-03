package ec.edu.ups.icc.fundamentos01.products.dtos;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDto {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private UserResponseDto owner;
    private List<CategoryResponseDto> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponseDto() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public UserResponseDto getOwner() { return owner; }
    public void setOwner(UserResponseDto owner) { this.owner = owner; }
    public List<CategoryResponseDto> getCategories() { return categories; }
    public void setCategories(List<CategoryResponseDto> categories) { this.categories = categories; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}