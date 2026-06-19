package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;

public interface UserService {

    List<UserResponseDto> findAll();

    Object findOne(Long id);

    UserResponseDto create(CreateUserDto dto);

    Object update(Long id, UpdateUserDto dto);

    Object partialUpdate(Long id, PartialUpdateUserDto dto);

    Object delete(Long id);
}