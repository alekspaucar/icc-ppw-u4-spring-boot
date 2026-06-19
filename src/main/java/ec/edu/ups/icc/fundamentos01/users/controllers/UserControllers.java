package ec.edu.ups.icc.fundamentos01.users.controllers;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.services.UserService;

@RestController
@RequestMapping("/users")
public class UserControllers {


@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserService service;

    public UsersController(UserService service) {
        this.service = service;
    }


    @GetMapping
    public List<UserResponseDto> findAll() {
        return service.findAll();
    }


    @GetMapping("/{id}")
    public Object findOne(@PathVariable Long id) {
        return service.findOne(id);
    }


    @PostMapping
    public UserResponseDto create(@RequestBody CreateUserDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public Object update(
            @PathVariable Long id,
            @RequestBody UpdateUserDto dto
    ) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    public Object partialUpdate(
            @PathVariable Long id,
            @RequestBody PartialUpdateUserDto dto
    ) {
        return service.partialUpdate(id, dto);
    }


    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
}
