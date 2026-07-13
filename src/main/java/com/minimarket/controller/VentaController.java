package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import com.minimarket.dto.VentaResponseDTO;
import com.minimarket.dto.ErrorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Venta", description = "API para gestionar ventas")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Error de validaciÃ³n o solicitud incorrecta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
})
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping("/checkout/usuario/{usuarioId}")
    @PreAuthorize("hasRole('CAJERO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Procesar venta desde carrito")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venta exitosa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponseDTO.class), examples = @ExampleObject(value = """
{
  "id": 1,
  "total": 5000.0,
  "usuarioId": 1,
  "_links": {
    "self": { "href": "http://localhost:8080/api/ventas/1" }
  }
}
""")))
    })
    public ResponseEntity<VentaResponseDTO> procesarVentaDesdeCarrito(@Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        Venta venta = ventaService.crearVentaDesdeCarrito(usuarioId);
        return ResponseEntity.ok(VentaResponseDTO.from(venta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener venta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venta encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponseDTO.class), examples = @ExampleObject(value = """
{
  "id": 1,
  "total": 5000.0,
  "usuarioId": 1,
  "_links": {
    "self": { "href": "http://localhost:8080/api/ventas/1" }
  }
}
"""))),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<VentaResponseDTO> obtenerPorId(@Parameter(description = "ID de la venta") @PathVariable Long id) {
        Venta v = ventaService.obtenerPorId(id);
        return v != null ? ResponseEntity.ok(VentaResponseDTO.from(v)) : ResponseEntity.notFound().build();
    }

    @GetMapping("/historial/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener historial de ventas de un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial obtenido", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<VentaResponseDTO>> obtenerHistorial(@Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        List<VentaResponseDTO> historial = ventaService.obtenerHistorialUsuario(usuarioId).stream().map(VentaResponseDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(historial);
    }
}