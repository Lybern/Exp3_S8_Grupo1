package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.minimarket.entity.Usuario;

@Schema(description = "Objeto de transferencia de datos para la creación y actualización de un Usuario")
public class UsuarioRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Schema(description = "Nombre de usuario único para inicio de sesión", example = "juanperez", required = true)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña del usuario", example = "Password123!", required = true)
    private String password;

    @Schema(description = "Nombre real del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Perez")
    private String apellido;

    @Email(message = "Debe ser un formato de correo válido")
    @Schema(description = "Correo electrónico de contacto", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "Dirección física del usuario", example = "Av. Siempre Viva 123")
    private String direccion;

    public UsuarioRequestDTO() {}

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Usuario toEntity() {
        Usuario u = new Usuario();
        u.setUsername(this.username);
        u.setPassword(this.password);
        u.setNombre(this.nombre);
        u.setApellido(this.apellido);
        u.setEmail(this.email);
        u.setDireccion(this.direccion);
        return u;
    }
}
