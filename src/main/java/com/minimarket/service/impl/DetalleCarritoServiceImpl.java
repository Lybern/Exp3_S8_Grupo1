package com.minimarket.service.impl;

import com.minimarket.entity.DetalleCarrito;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.DetalleCarritoRepository;
import com.minimarket.service.DetalleCarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetalleCarritoServiceImpl implements DetalleCarritoService {

    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;

    @Override
    @Transactional(readOnly = true)
    public DetalleCarrito findById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID del detalle del carrito no puede ser nulo.");
        }
        return detalleCarritoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El ítem del carrito con ID " + id + " no existe."));
    }

    /**
     * Actualiza la cantidad de un ítem específico en el carrito, 
     * asegurando que haya stock suficiente en el producto.
     */
    @Override
    @Transactional
    public DetalleCarrito actualizarCantidad(Long detalleId, Integer nuevaCantidad) {
        if (nuevaCantidad == null || nuevaCantidad <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor a cero.");
        }

        // Buscamos el detalle actual usando el método protegido de arriba para heredar el 404 dinámico
        DetalleCarrito detalle = this.findById(detalleId);
        
        // Validación de negocio: Verificar el stock directo desde el objeto Producto asociado
        if (detalle.getProducto() == null) {
            throw new NotFoundException("El producto asociado a este detalle de carrito ya no existe.");
        }

        if (detalle.getProducto().getStock() < nuevaCantidad) {
            throw new BadRequestException("Stock insuficiente para el producto: " 
                    + detalle.getProducto().getNombre() 
                    + ". Stock disponible: " + detalle.getProducto().getStock());
        }

        // Modificamos la cantidad en la entidad gestionada por JPA
        detalle.setCantidad(nuevaCantidad);
        
        // Guardamos y retornamos el registro actualizado
        return detalleCarritoRepository.save(detalle);
    }

    /**
     * Elimina permanentemente una línea de producto (ítem) del carrito.
     */
    @Override
    @Transactional
    public void eliminarItem(Long detalleId) {
        // Nos aseguramos de que el ítem exista antes de intentar borrarlo para lanzar un 404 controlado
        DetalleCarrito detalle = this.findById(detalleId);
        detalleCarritoRepository.delete(detalle);
    }
}