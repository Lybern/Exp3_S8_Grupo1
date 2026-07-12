package com.minimarket.controller;

import com.minimarket.entity.DetalleCarrito;
import com.minimarket.service.DetalleCarritoService;
import com.minimarket.dto.DetalleCarritoResponseDTO;
import com.minimarket.dto.ErrorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/detalle-carrito")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
@Tag(name = "Detalle Carrito", description = "API para gestionar items del carrito")
public class DetalleCarritoController {

    @Autowired
    private DetalleCarritoService detalleCarritoService;

    @PutMapping("/{id}/cantidad")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar cantidad de un item")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cantidad actualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleCarritoResponseDTO.class)))
    })
    public ResponseEntity<DetalleCarritoResponseDTO> actualizarCantidad(
            @Parameter(description = "ID del detalle") @PathVariable Long id,
            @Parameter(description = "Nueva cantidad") @RequestParam Integer nuevaCantidad) {
        DetalleCarrito detalle = detalleCarritoService.actualizarCantidad(id, nuevaCantidad);
        return ResponseEntity.ok(DetalleCarritoResponseDTO.from(detalle));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar item del carrito")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item eliminado"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminarItem(@Parameter(description = "ID del detalle") @PathVariable Long id) {
        detalleCarritoService.eliminarItem(id);
        return ResponseEntity.ok().build();
    }
}