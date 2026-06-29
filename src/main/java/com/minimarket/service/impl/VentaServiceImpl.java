package com.minimarket.service.impl;

import com.minimarket.entity.*;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    private static final int UMBRAL_MINIMO_STOCK = 5;

    /**
     * Procesa la facturación y generación de una venta a partir del carrito activo.
     * Requiere que el usuario autenticado posea el rol de CAJERO.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('CAJERO')")
    public Venta crearVentaDesdeCarrito(Long usuarioId) {
        if (usuarioId == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }

        // 1. Obtener el carrito actual del cliente
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NotFoundException("El usuario no tiene un carrito activo."));

        if (carrito.getItems().isEmpty()) {
            throw new BadRequestException("No se puede procesar una venta con el carrito vacío.");
        }

        // 2. Crear la cabecera de la Venta (Boleta)
        Venta venta = new Venta();
        venta.setUsuario(carrito.getUsuario());
        venta.setFecha(new Date());
        venta.setTotal(0.0);

        // 3. Pasar cada ítem del Carrito al Detalle de la Venta
        for (DetalleCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();
            int cantidadComprada = itemCarrito.getCantidad();

            // Validación constante de stock (Seguridad concurrente)
            if (producto.getStock() < cantidadComprada) {
                throw new BadRequestException("Stock insuficiente de última hora para el producto: " 
                        + producto.getNombre() + ". Disponible: " + producto.getStock());
            }

            // Descontar stock real
            int nuevoStock = producto.getStock() - cantidadComprada;
            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            // Registro preciso en el Kardex (Movimientos)
            Inventario movimiento = new Inventario();
            movimiento.setProducto(producto);
            movimiento.setCantidad(cantidadComprada);
            movimiento.setTipoMovimiento("SALIDA_VENTA");
            movimiento.setFechaMovimiento(new Date());
            inventarioRepository.save(movimiento);

            // Alertas automatizadas de reabastecimiento por umbral mínimo
            if (nuevoStock <= UMBRAL_MINIMO_STOCK) {
                System.out.println("[ALERT - NOTIFICACIÓN MINIMARKET PLUS] El producto '" 
                        + producto.getNombre() + "' ha alcanzado o superado el umbral mínimo (<= " 
                        + UMBRAL_MINIMO_STOCK + "). Stock crítico actual: " + nuevoStock 
                        + ". Emitir orden de reabastecimiento.");
            }

            // Detalle de venta con cantidades y precios congelados
            DetalleVenta detalleVenta = new DetalleVenta();
            detalleVenta.setVenta(venta);
            detalleVenta.setProducto(producto);
            detalleVenta.setCantidad(cantidadComprada);
            detalleVenta.setPrecio(producto.getPrecio());

            // Acumular la línea en la boleta
            venta.getDetalles().add(detalleVenta);
            
            // Calcular el total acumulado de la boleta
            venta.setTotal(venta.getTotal() + (detalleVenta.getPrecio() * cantidadComprada));
        }

        // 4. Guardar la venta (Se guarda en cascada la cabecera y sus detalles)
        Venta ventaGuardada = ventaRepository.save(venta);

        // 5. Vaciar y resetear de forma atómica el carrito para su próximo uso
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return ventaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> obtenerHistorialUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        return ventaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorId(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de la venta no puede ser nulo.");
        }
        return ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La venta con ID " + id + " no existe."));
    }
}