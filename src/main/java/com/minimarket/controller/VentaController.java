package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // 🚨 REQUERIMIENTO DEL CASO: Solo el cajero genera la venta en el mesón
    @PostMapping("/checkout/usuario/{usuarioId}")
    @PreAuthorize("hasRole('CAJERO')")
    public ResponseEntity<Venta> procesarVentaDesdeCarrito(@PathVariable Long usuarioId) {
        Venta venta = ventaService.crearVentaDesdeCarrito(usuarioId);
        return ResponseEntity.ok(venta);
    }

    // Permitido para administración o revisión del cajero
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/historial/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
    public ResponseEntity<List<Venta>> obtenerHistorial(@PathVariable Long usuarioId) {
        List<Venta> historial = ventaService.obtenerHistorialUsuario(usuarioId);
        return ResponseEntity.ok(historial);
    }
}