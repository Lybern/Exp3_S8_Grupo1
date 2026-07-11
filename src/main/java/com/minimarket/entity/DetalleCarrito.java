package com.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
/**
 * Entidad intermedia que resuelve la relación N:M entre Carrito y Producto.
 * Esta clase fue introducida para solucionar la limitación transaccional
 * que duplicaba registros por cada artículo agregado al carrito.
 */
public class DetalleCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación ManyToOne hacia la cabecera del Carrito
    @ManyToOne
    @JoinColumn(name = "carrito_id", nullable = false)
    @JsonIgnore
    private Carrito carrito;

    // Relación ManyToOne hacia el Producto
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // La columna adicional en la tabla intermedia que actúa como contador
    @Column(nullable = false)
    private Integer cantidad;

    // Constructor vacío requerido por JPA
    public DetalleCarrito() {}

    // Constructor de conveniencia utilizado en el método agregarProducto
    public DetalleCarrito(Carrito carrito, Producto producto, Integer cantidad) {
        this.carrito = carrito;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void setCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}