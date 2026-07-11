package com.minimarket.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un usuario solo puede tener un carrito activo a la vez
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @JsonIgnore
    private Usuario usuario;

    // Relación bidireccional con la tabla intermedia (DetalleCarrito)
    // cascade = CascadeType.ALL guarda/borra los detalles automáticamente al guardar/borrar el carrito
    // orphanRemoval = true elimina de la BD los ítems que quites de esta lista (SON CARRITOS TEMPORALES)
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCarrito> items = new ArrayList<>();

    public Carrito() {}

    /**
     * Lógica de negocio para agregar un producto al carrito.
     * Si el producto ya existe, aumenta el contador en la tabla intermedia.
     * Valida que la cantidad solicitada no supere el stock disponible.
     */
    public void agregarProducto(Producto producto, Integer cantidadAAgregar) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (cantidadAAgregar == null || cantidadAAgregar <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (producto.getStock() == null) {
            throw new IllegalArgumentException("El producto no tiene stock definido");
        }

        // 1. Buscar si el producto ya existe en la tabla intermedia (Detalle)
        for (DetalleCarrito item : this.items) {
            if (item.getProducto().getId().equals(producto.getId())) {
                int nuevaCantidadTotal = item.getCantidad() + cantidadAAgregar;
                
                // Validar stock con el acumulado
                if (producto.getStock() < nuevaCantidadTotal) {
                    throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStock());
                }
                
                // Aumentar el contador
                item.setCantidad(nuevaCantidadTotal);
                return;
            }
        }

        // 2. Si es un producto nuevo en el carrito, validar stock inicial
        if (producto.getStock() < cantidadAAgregar) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        // Crear el nuevo registro para la tabla intermedia
        DetalleCarrito nuevoItem = new DetalleCarrito(this, producto, cantidadAAgregar);
        this.items.add(nuevoItem);
    }


    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetalleCarrito> getItems() {
        return items;
    }

    public void setItems(List<DetalleCarrito> items) {
        this.items = items;
    }
}