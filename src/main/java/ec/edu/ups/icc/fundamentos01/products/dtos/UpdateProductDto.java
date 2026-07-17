package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos requeridos para actualizar completamente un producto")
public class UpdateProductDto {

    @Schema(description = "Nombre del producto", example = "Laptop Lenovo ThinkPad")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String name;

    @Schema(description = "Precio del producto", example = "899.99")
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double price;

    @Schema(description = "Cantidad disponible en inventario", example = "10")
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Schema(description = "IDs de las categorías asociadas al producto", example = "[1, 2]")
    @NotEmpty(message = "Debe seleccionar al menos una categoria")
    private Set<Long> categoryIds;

    @Schema(description = "Descripción detallada del producto", example = "Laptop empresarial con procesador Intel Core i7")
    @Size(max = 250, message = "La descripción no puede tener más de 250 caracteres")
    private String description;
    
    public UpdateProductDto() {
    }

    public UpdateProductDto(String name, Double price, Integer stock, Set<Long> categoryIds, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryIds = categoryIds;
        this.description = description; 
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
}