package com.minimarket.repository;

import com.minimarket.entity.DetalleCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    
    // Método útil para buscar si un producto específico ya existe en el carrito de un usuario
    Optional<DetalleCarrito> findByCarritoIdAndProductoId(Long carritoId, Long productoId);
}