package com.minimarket.service;

import com.minimarket.entity.DetalleCarrito;

public interface DetalleCarritoService {
    DetalleCarrito findById(Long id);
    DetalleCarrito actualizarCantidad(Long detalleId, Integer nuevaCantidad);
    void eliminarItem(Long detalleId);
}