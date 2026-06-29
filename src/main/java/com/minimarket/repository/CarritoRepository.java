package com.minimarket.repository;

import com.minimarket.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    
    // Cambiamos List<Carrito> por Optional<Carrito> ya que es un único carro por usuario
    Optional<Carrito> findByUsuarioId(Long usuarioId);
}