package com.minimarket.service;

import com.minimarket.entity.Carrito;

public interface CarritoService {
    Carrito obtenerOCrearCarrito(Long usuarioId);
    Carrito agregarProductoAlCarrito(Long usuarioId, Long productoId, Integer cantidad);
    Carrito eliminarItemDelCarrito(Long usuarioId, Long detalleId);
    void vaciarCarrito(Long usuarioId);
}