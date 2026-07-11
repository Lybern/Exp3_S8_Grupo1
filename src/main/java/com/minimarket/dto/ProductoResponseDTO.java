package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import com.minimarket.entity.Categoria;

@Schema(description = "DTO para representar un Producto con HATEOAS")
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Categoria categoria;

    @JsonProperty("_links")
    @Schema(description = "Enlaces a operaciones sobre el producto")
    private Map<String, ProductoPageResponseDTO.Link> links;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Map<String, ProductoPageResponseDTO.Link> getLinks() { return links; }
    public void setLinks(Map<String, ProductoPageResponseDTO.Link> links) { this.links = links; }
}
