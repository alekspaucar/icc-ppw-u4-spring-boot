package ec.edu.ups.icc.fundamentos01.users.mappers;

import java.time.LocalDateTime;

import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.models.UserModel;

public class UserMapper {

      public static UserModel toModel(CreateUserDto dto) {

        UserModel model = new UserModel();

        model.setName(dto.getName());
        model.setEmail(dto.getEmail());
        model.setPassword(dto.getPassword());

        model.setPasswordHash("HASH_" + dto.getPassword());
        model.setCreatedAt(LocalDateTime.now());

        return model;
    }


    public static UserResponseDto toResponse(UserModel model) {

        UserResponseDto response = new UserResponseDto();

        response.setId(model.getId());
        response.setName(model.getName());
        response.setEmail(model.getEmail());

        return response;
    }

    
}