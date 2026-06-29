package com.minimarket.repository;

import com.minimarket.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Para mostrarle al usuario su historial de boletas/compras
    List<Venta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}