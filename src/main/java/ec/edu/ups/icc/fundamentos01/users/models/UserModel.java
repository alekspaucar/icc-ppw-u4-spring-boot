package ec.edu.ups.icc.fundamentos01.users.models;

import java.time.LocalDateTime;

public class UserModel {

    private Long id;

    private String name;

    private String email;

    private LocalDateTime createdAt;

    private String password;

    private String passwordHash;

    public UserModel() {
    }

    public UserModel(Long id, String name, String email, String password, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // getters y setters

}