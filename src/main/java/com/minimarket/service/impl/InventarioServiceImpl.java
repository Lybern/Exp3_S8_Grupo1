package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Inventario findById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de movimiento de inventario no puede ser nulo.");
        }
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El registro de inventario con ID " + id + " no existe."));
    }

    /**
     * Registra un nuevo movimiento de inventario (Kardex) en el sistema.
     * Requiere que el usuario autenticado posea el rol de ADMINISTRADOR.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Inventario save(Inventario inventario) {
        if (inventario == null || inventario.getProducto() == null) {
            throw new BadRequestException("Los datos del movimiento de inventario son inválidos.");
        }
        return inventarioRepository.save(inventario);
    }

    /**
     * Elimina un registro de movimiento de inventario por su identificador único.
     * Requiere que el usuario autenticado posea el rol de ADMINISTRADOR.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void deleteById(Long id) {
        Inventario inventario = this.findById(id);
        inventarioRepository.delete(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> findByProductoId(Long productoId) {
        if (productoId == null) {
            throw new BadRequestException("El ID del producto no puede ser nulo.");
        }
        return inventarioRepository.findByProductoId(productoId);
    }
}