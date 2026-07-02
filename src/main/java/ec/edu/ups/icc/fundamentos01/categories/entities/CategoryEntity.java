package ec.edu.ups.icc.fundamentos01.categories.entities;

import ec.edu.ups.icc.fundamentos01.core.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    // Constructor vacío
    public CategoryEntity() {
    }
    // Constructor lleno
    public CategoryEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters y setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}