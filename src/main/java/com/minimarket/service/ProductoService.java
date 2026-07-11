package com.minimarket.service;

import com.minimarket.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductoService {
    Page<Producto> findAll(Pageable pageable);
    Producto findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
    List<Producto> findByCategoriaId(Long categoriaId);
    Producto modificarProducto(Long id, Producto productoDetalles);
}
