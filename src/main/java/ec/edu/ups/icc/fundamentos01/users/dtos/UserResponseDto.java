package ec.edu.ups.icc.fundamentos01.users.dtos;

public class UserResponseDto {
    private String name;
    private Long id;
    private String email;

    public UserResponseDto() {
    }

    public UserResponseDto(String name, Long id, String email) {
        this.name = name;
        this.id = id;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
