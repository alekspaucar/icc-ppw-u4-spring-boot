package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.core.dto.ErrorResponseDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.mappers.UserMapper;
import ec.edu.ups.icc.fundamentos01.users.models.UserModel;

@Service
public class UserServiceImpl implements UserService {

    private List<UserModel> users = new ArrayList<>();
    private Long currentId = 1L;

    @Override
    public List<UserResponseDto> findAll() {
        return users.stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public Object findOne(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .map(user -> (Object) UserMapper.toResponse(user))
                .orElseGet(() -> new ErrorResponseDto("User not found"));
    }

    @Override
    public UserResponseDto create(CreateUserDto dto) {
        UserModel user = UserMapper.toModel(dto);
        user.setId(currentId);
        currentId++;
        users.add(user);
        return UserMapper.toResponse(user);
    }

    @Override
    public Object update(Long id, UpdateUserDto dto) {
        UserModel user = users.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (user == null) {
            return new ErrorResponseDto("User not found");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return UserMapper.toResponse(user);
    }

    @Override
    public Object partialUpdate(Long id, PartialUpdateUserDto dto) {
        UserModel user = users.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (user == null) {
            return new ErrorResponseDto("User not found");
        }

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        return UserMapper.toResponse(user);
    }

    @Override
    public Object delete(Long id) {
        boolean removed = users.removeIf(user -> user.getId().equals(id));

        if (!removed) {
            return new ErrorResponseDto("User not found");
        }

        return new Object() {
            public String message = "Deleted successfully";
        };
    }
}