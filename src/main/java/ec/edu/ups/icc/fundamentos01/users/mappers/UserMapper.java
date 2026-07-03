package ec.edu.ups.icc.fundamentos01.users.mappers;

import java.time.LocalDateTime;

import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.models.UserModel;

public class UserMapper {

    public static UserModel toModelFromDTO(CreateUserDto dto) {
        UserModel model = new UserModel();
        model.setName(dto.getName());
        model.setEmail(dto.getEmail());
        model.setPassword(dto.getPassword());
        model.setPasswordHash("HASH_" + dto.getPassword());
        model.setCreatedAt(LocalDateTime.now());
        return model;
    }

    public static UserModel toModelFromEntity(UserEntity entity) {
        UserModel model = new UserModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setEmail(entity.getEmail());
        model.setPasswordHash(entity.getPasswordHash());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setDeleted(entity.isDeleted());
        return model;
    }

    public static UserEntity toEntityFromModel(UserModel model) {
        UserEntity entity = new UserEntity();
        entity.setName(model.getName());
        entity.setEmail(model.getEmail());
        entity.setPasswordHash(model.getPasswordHash());
        return entity;
    }

    public static UserResponseDto toResponse(UserModel model) {
        UserResponseDto response = new UserResponseDto();
        response.setName(model.getName());
        response.setId(model.getId());
        response.setEmail(model.getEmail());
        return response;
    }
}
