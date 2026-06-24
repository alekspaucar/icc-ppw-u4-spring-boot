package ec.edu.ups.icc.fundamentos01.products.dtos;
import jakarta.validation.constraints.*;
public class UpdateProductDto {
    @NotBlank(message="El nombre es obligatorio") @Size(min=3,max=150,message="El nombre debe tener entre 3 y 150 caracteres") private String name;
    @NotNull(message="El precio es obligatorio") @DecimalMin(value="0.0",message="El precio no puede ser negativo") private Double price;
    @NotNull(message="El stock es obligatorio") @Min(value=0,message="El stock no puede ser negativo") private Integer stock;
    public UpdateProductDto() {}
    public String getName() { return name; } public void setName(String n) { this.name=n; }
    public Double getPrice() { return price; } public void setPrice(Double p) { this.price=p; }
    public Integer getStock() { return stock; } public void setStock(Integer s) { this.stock=s; }
}