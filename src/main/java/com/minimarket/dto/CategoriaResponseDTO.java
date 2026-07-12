package com.minimarket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import com.minimarket.entity.Categoria;
@Schema(description = "DTO de respuesta para Categoria")
public class CategoriaResponseDTO {
    @Schema(description = "ID de la categoria", example = "1")
    private Long id;
    @Schema(description = "Nombre de la categoria", example = "Lácteos")
    private String nombre;
    public CategoriaResponseDTO(Long id, String nombre) { this.id = id; this.nombre = nombre; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public static CategoriaResponseDTO from(Categoria c) {
        if(c == null) return null;
        return new CategoriaResponseDTO(c.getId(), c.getNombre());
    }
}
