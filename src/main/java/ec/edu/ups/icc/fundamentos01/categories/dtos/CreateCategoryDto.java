package ec.edu.ups.icc.fundamentos01.categories.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCategoryDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
    private String description;


    public CreateCategoryDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}