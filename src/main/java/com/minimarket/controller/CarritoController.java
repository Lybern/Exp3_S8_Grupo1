package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.exception.BadRequestException;
import com.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // Cualquier usuario autenticado puede obtener o crear su propio carrito
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
    public ResponseEntity<Carrito> obtenerCarrito(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.obtenerOCrearCarrito(usuarioId);
        return ResponseEntity.ok(carrito);
    }

    // Agregar productos al carrito
    @PostMapping("/usuario/{usuarioId}/agregar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
    public ResponseEntity<Carrito> agregarProducto(
            @PathVariable Long usuarioId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
        Carrito carrito = carritoService.agregarProductoAlCarrito(usuarioId, productoId, cantidad);
        return ResponseEntity.ok(carrito);
    }

    // Eliminar una línea completa del carrito (DetalleCarrito)
    @DeleteMapping("/usuario/{usuarioId}/item/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
    public ResponseEntity<Carrito> eliminarItem(
            @PathVariable Long usuarioId,
            @PathVariable Long detalleId) {
        Carrito carrito = carritoService.eliminarItemDelCarrito(usuarioId, detalleId);
        return ResponseEntity.ok(carrito);
    }

    // Vaciar el carrito por completo
    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
    public ResponseEntity<Map<String, String>> vaciarCarrito(@PathVariable Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado correctamente."));
    }
}