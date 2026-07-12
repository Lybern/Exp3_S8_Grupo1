package com.minimarket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import com.minimarket.entity.Inventario;
import java.util.Date;
@Schema(description = "DTO de respuesta para movimiento de inventario")
public class InventarioResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private String tipoMovimiento;
    private Date fechaMovimiento;
    public InventarioResponseDTO(Long id, Long productoId, String productoNombre, Integer cantidad, String tipoMovimiento, Date fechaMovimiento) {
        this.id = id; this.productoId = productoId; this.productoNombre = productoNombre;
        this.cantidad = cantidad; this.tipoMovimiento = tipoMovimiento; this.fechaMovimiento = fechaMovimiento;
    }
    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public String getProductoNombre() { return productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public Date getFechaMovimiento() { return fechaMovimiento; }
    public static InventarioResponseDTO from(Inventario i) {
        if(i == null) return null;
        return new InventarioResponseDTO(i.getId(), i.getProducto().getId(), i.getProducto().getNombre(), i.getCantidad(), i.getTipoMovimiento(), i.getFechaMovimiento());
    }
}
