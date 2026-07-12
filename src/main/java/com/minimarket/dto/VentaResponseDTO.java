package com.minimarket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import com.minimarket.entity.Venta;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Schema(description = "DTO de respuesta para Venta")
public class VentaResponseDTO {
    private Long id;
    private Long usuarioId;
    private Date fecha;
    private Double total;
    public VentaResponseDTO(Long id, Long usuarioId, Date fecha, Double total) {
        this.id = id; this.usuarioId = usuarioId; this.fecha = fecha; this.total = total;
    }
    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Date getFecha() { return fecha; }
    public Double getTotal() { return total; }
    public static VentaResponseDTO from(Venta v) {
        if(v == null) return null;
        return new VentaResponseDTO(v.getId(), v.getUsuario().getId(), v.getFecha(), v.getTotal());
    }
}
