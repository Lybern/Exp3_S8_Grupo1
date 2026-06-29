package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')") // Los clientes jamás ven el stock interno
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Inventario>> listarMovimientos() {
        return ResponseEntity.ok(inventarioService.findAll());
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<Inventario>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.findByProductoId(productoId));
    }

    // Registrar entradas manuales de mercadería o auditorías de stock
    @PostMapping
    public ResponseEntity<Inventario> registrarMovimiento(@RequestBody Inventario inventario) {
        return ResponseEntity.ok(inventarioService.save(inventario));
    }
}