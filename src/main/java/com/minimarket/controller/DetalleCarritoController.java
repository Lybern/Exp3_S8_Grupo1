package com.minimarket.controller;

import com.minimarket.entity.DetalleCarrito;
import com.minimarket.service.DetalleCarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detalle-carrito")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
public class DetalleCarritoController {

    @Autowired
    private DetalleCarritoService detalleCarritoService;

    // Actualiza la cantidad de un ítem validando stock en tiempo real
    @PutMapping("/{id}/cantidad")
    public ResponseEntity<DetalleCarrito> actualizarCantidad(
            @PathVariable Long id,
            @RequestParam Integer nuevaCantidad) {
        DetalleCarrito detalle = detalleCarritoService.actualizarCantidad(id, nuevaCantidad);
        return ResponseEntity.ok(detalle);
    }

    // Eliminar el ítem directamente por su ID de detalle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        detalleCarritoService.eliminarItem(id);
        return ResponseEntity.noContent().build();
    }
}