package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Carrito obtenerOCrearCarrito(Long usuarioId) {
        if (usuarioId == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new NotFoundException("El usuario con ID " + usuarioId + " no existe."));
                    
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Override
    @Transactional
    public Carrito agregarProductoAlCarrito(Long usuarioId, Long productoId, Integer cantidad) {
        if (productoId == null || cantidad == null || cantidad <= 0) {
            throw new BadRequestException("El producto y una cantidad válida mayor a cero son obligatorios.");
        }
        
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NotFoundException("El producto con ID " + productoId + " no existe."));

        // Lanza IllegalArgumentException nativo de la entidad si falta stock, el cual también podemos atrapar o mapear
        carrito.agregarProducto(producto, cantidad);

        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public Carrito eliminarItemDelCarrito(Long usuarioId, Long detalleId) {
        if (detalleId == null) {
            throw new BadRequestException("El ID de detalle no puede ser nulo.");
        }
        
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        
        boolean removido = carrito.getItems().removeIf(item -> item.getId().equals(detalleId));
        
        if (!removido) {
            throw new NotFoundException("El ítem con ID " + detalleId + " no pertenece al carrito del usuario.");
        }

        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }
}