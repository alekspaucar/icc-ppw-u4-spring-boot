package ec.edu.ups.icc.fundamentos01.categories.dtos;

public class UpdateCategoryDto {
    private String name;
    private String description;

    // Constructors
    public UpdateCategoryDto() {
    }

    public UpdateCategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and setters
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
    