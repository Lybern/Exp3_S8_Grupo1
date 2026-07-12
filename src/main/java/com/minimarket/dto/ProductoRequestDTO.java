package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Categoria;

@Schema(description = "Objeto de transferencia de datos para la creación y actualización de un Producto")
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Schema(description = "Nombre descriptivo del producto", example = "Leche Entera", required = true)
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un valor positivo")
    @Schema(description = "Precio del producto en la moneda local", example = "1200.50", required = true)
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @Schema(description = "Cantidad disponible en inventario", example = "50", required = true)
    private Integer stock;

    @NotNull(message = "El ID de la categoría es obligatorio")
    @Schema(description = "Identificador de la categoría a la que pertenece el producto", example = "1", required = true)
    private Long categoriaId;

    public ProductoRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public Producto toEntity() {
        Producto p = new Producto();
        p.setNombre(this.nombre);
        p.setPrecio(this.precio);
        p.setStock(this.stock);
        if (this.categoriaId != null) {
            Categoria c = new Categoria();
            c.setId(this.categoriaId);
            p.setCategoria(c);
        }
        return p;
    }
}
