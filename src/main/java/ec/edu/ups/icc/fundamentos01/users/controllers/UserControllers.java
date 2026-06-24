package ec.edu.ups.icc.fundamentos01.users.controllers;
import ec.edu.ups.icc.fundamentos01.users.dtos.*;
import ec.edu.ups.icc.fundamentos01.users.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/users")
public class UserControllers {
    private final UserService service;
    public UserControllers(UserService service) { this.service = service; }
    @GetMapping public List<UserResponseDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public UserResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }
    @PostMapping public UserResponseDto create(@Valid @RequestBody CreateUserDto dto) { return service.create(dto); }
    @PutMapping("/{id}") public UserResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto dto) { return service.update(id, dto); }
    @PatchMapping("/{id}") public UserResponseDto partialUpdate(@PathVariable Long id, @Valid @RequestBody PartialUpdateUserDto dto) { return service.partialUpdate(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}