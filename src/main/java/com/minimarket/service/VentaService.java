package com.minimarket.service;

import com.minimarket.entity.Venta;
import java.util.List;

public interface VentaService {
    Venta crearVentaDesdeCarrito(Long usuarioId);
    List<Venta> obtenerHistorialUsuario(Long usuarioId);
    Venta obtenerPorId(Long id);
}