package ec.edu.ups.icc.fundamentos01.products.dtos;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
/*
 * DTO utilizado para actualizar parcialmente un producto.
 *
 * Solo se actualizan los campos enviados.
 */
public class PartialUpdateProductDto {

    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String name;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private Double price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private Long categoryId;

    // Constructor vacío
    public PartialUpdateProductDto() {}

    // Constructor lleno
    public PartialUpdateProductDto(String name, Double price, Integer stock, Long categoryId) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
    }
    // Getters y setters
    
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
}