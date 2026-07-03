package ec.edu.ups.icc.fundamentos01.products.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public class ProductFilterByUserDto {

    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    private String name;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio minimo no puede ser negativo")
    private Double minPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio maximo no puede ser negativo")
    private Double maxPrice;

    public boolean hasValidPriceRange() {
        if (minPrice != null && maxPrice != null) return maxPrice >= minPrice;
        return true;
    }

    public boolean hasEmptyName() { return name == null || name.isBlank(); }

    public ProductFilterByUserDto() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
}