package com.minimarket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import com.minimarket.entity.DetalleCarrito;
@Schema(description = "DTO de respuesta para Detalle del Carrito")
public class DetalleCarritoResponseDTO {
    private Long id;
    private Long productoId;
    private Integer cantidad;
    private Double subtotal;
    public DetalleCarritoResponseDTO(Long id, Long productoId, Integer cantidad, Double subtotal) {
        this.id = id; this.productoId = productoId; this.cantidad = cantidad; this.subtotal = subtotal;
    }
    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public Integer getCantidad() { return cantidad; }
    public Double getSubtotal() { return subtotal; }
    public static DetalleCarritoResponseDTO from(DetalleCarrito d) {
        if(d == null) return null;
        return new DetalleCarritoResponseDTO(d.getId(), d.getProducto().getId(), d.getCantidad(), d.getProducto().getPrecio() * d.getCantidad());
    }
}
