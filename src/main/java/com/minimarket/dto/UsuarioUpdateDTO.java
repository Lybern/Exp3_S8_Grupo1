package com.minimarket.dto;

import java.util.Set;

public class UsuarioUpdateDTO {
    private String username;
    private Set<String> roles; // Nombres de los roles a asignar (ej: ["VENDEDOR"])

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}