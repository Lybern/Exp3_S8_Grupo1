package com.minimarket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import java.util.Date;
@Schema(description = "DTO para registrar movimiento de inventario")
public class InventarioRequestDTO {
    @NotNull(message = "El ID del producto es obligatorio")
    @Schema(description = "ID del producto", example = "1", required = true)
    private Long productoId;
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Schema(description = "Cantidad del movimiento", example = "50", required = true)
    private Integer cantidad;
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Schema(description = "Tipo de movimiento (ej. ENTRADA, SALIDA)", example = "ENTRADA", required = true)
    private String tipoMovimiento;
    @NotNull(message = "La fecha es obligatoria")
    @Schema(description = "Fecha del movimiento", example = "2023-10-01T12:00:00Z", required = true)
    private Date fechaMovimiento;
    public InventarioRequestDTO() {}
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public Date getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(Date fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public Inventario toEntity() {
        Inventario i = new Inventario();
        Producto p = new Producto(); p.setId(this.productoId);
        i.setProducto(p);
        i.setCantidad(this.cantidad);
        i.setTipoMovimiento(this.tipoMovimiento);
        i.setFechaMovimiento(this.fechaMovimiento);
        return i;
    }
}
