package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.util.Set;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class PartialUpdateProductDto {
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String name;

    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Size(max = 300, message = "La descripción no puede tener más de 300 caracteres")
    private String description;

    private Set<Long> categoryIds;

    public PartialUpdateProductDto(String name, Double price, Integer stock, Set<Long> categoryIds, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryIds = categoryIds;
        this.description = description; 
    }
    public PartialUpdateProductDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double p) {
        this.price = p;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer s) {
        this.stock = s;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> c) {
        this.categoryIds = c;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}