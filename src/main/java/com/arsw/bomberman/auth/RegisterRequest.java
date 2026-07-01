package com.arsw.bomberman.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

    @NotBlank(message = "username es requerido")
    private String username;

    @NotBlank(message = "email es requerido")
    @Email(message = "email no es válido")
    private String email;

    @NotBlank(message = "password es requerido")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}