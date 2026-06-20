package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;
import org.springframework.stereotype.Service;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.mappers.UserMapper;
import ec.edu.ups.icc.fundamentos01.users.models.UserModel;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toModelFromEntity)
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponseDto findOne(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toModelFromEntity)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @Override
    public UserResponseDto create(CreateUserDto dto) {
        UserModel model = UserMapper.toModelFromDTO(dto);
        UserEntity entity = UserMapper.toEntityFromModel(model);
        UserEntity saved = userRepository.save(entity);
        return UserMapper.toResponse(UserMapper.toModelFromEntity(saved));
    }

    @Override
    public UserResponseDto update(Long id, UpdateUserDto dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        UserEntity saved = userRepository.save(entity);
        return UserMapper.toResponse(UserMapper.toModelFromEntity(saved));
    }

    @Override
    public UserResponseDto partialUpdate(Long id, PartialUpdateUserDto dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        UserEntity saved = userRepository.save(entity);
        return UserMapper.toResponse(UserMapper.toModelFromEntity(saved));
    }

    @Override
    public void delete(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        entity.setDeleted(true);
        userRepository.save(entity);
    }
}
