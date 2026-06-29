package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.exception.NotFoundException;
import com.minimarket.exception.BadRequestException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del producto no puede ser nulo.");
        }
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El producto con ID " + id + " no existe."));
    }

    /**
     * Guarda un nuevo producto en el sistema.
     * Requiere que el usuario autenticado posea el rol de ADMINISTRADOR.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto save(Producto producto) {
        if (producto == null) {
            throw new BadRequestException("El producto no puede ser nulo.");
        }
        return productoRepository.save(producto);
    }

    /**
     * Modifica los datos de un producto existente en el catálogo.
     * Requiere que el usuario autenticado posea el rol de ADMINISTRADOR.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto modificarProducto(Long id, Producto productoDetalles) {
        if (id == null || productoDetalles == null) {
            throw new BadRequestException("El ID y los datos de actualización no pueden ser nulos.");
        }
        
        Producto productoExistente = this.findById(id);
        
        productoExistente.setNombre(productoDetalles.getNombre());
        productoExistente.setPrecio(productoDetalles.getPrecio());
        productoExistente.setStock(productoDetalles.getStock());
        productoExistente.setCategoria(productoDetalles.getCategoria());
        
        return productoRepository.save(productoExistente);
    }

    /**
     * Elimina un producto por su identificador único.
     * Requiere que el usuario autenticado posea el rol de ADMINISTRADOR.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void deleteById(Long id) {
        // Aseguramos que exista para lanzar 404 en vez de un error genérico de base de datos
        Producto producto = this.findById(id);
        productoRepository.delete(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByCategoriaId(Long categoriaId) {
        if (categoriaId == null) {
            throw new BadRequestException("El ID de la categoría no puede ser nulo.");
        }
        return productoRepository.findByCategoriaId(categoriaId);
    }
}