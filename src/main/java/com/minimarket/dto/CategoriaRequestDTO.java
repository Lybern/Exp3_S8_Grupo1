package com.minimarket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import com.minimarket.entity.Categoria;
@Schema(description = "DTO para crear o actualizar una Categoria")
public class CategoriaRequestDTO {
    @NotBlank(message = "El nombre no puede estar vacío")
    @Schema(description = "Nombre de la categoria", example = "Lácteos", required = true)
    private String nombre;
    public CategoriaRequestDTO() {}
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Categoria toEntity() {
        Categoria c = new Categoria();
        c.setNombre(this.nombre);
        return c;
    }
}
