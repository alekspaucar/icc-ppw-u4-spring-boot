package ec.edu.ups.icc.fundamentos01.users.controllers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.services.UserService;

@RestController
@RequestMapping("/users")
public class UserControllers {

    private final UserService service;

    public UserControllers(UserService service) { this.service = service; }

    @GetMapping
    public List<UserResponseDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public UserResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    public UserResponseDto create(@RequestBody CreateUserDto dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public UserResponseDto update(@PathVariable Long id, @RequestBody UpdateUserDto dto) { return service.update(id, dto); }

    @PatchMapping("/{id}")
    public UserResponseDto partialUpdate(@PathVariable Long id, @RequestBody PartialUpdateUserDto dto) { return service.partialUpdate(id, dto); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
