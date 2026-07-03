package ec.edu.ups.icc.fundamentos01.categories.mappers;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entities.CategoryEntity;

public class CategoryMapper {

    public static CategoryResponseDto toResponse(CategoryEntity entity) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}


