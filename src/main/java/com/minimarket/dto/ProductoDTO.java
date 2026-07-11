package com.minimarket.dto;

public class ProductoDTO {

    private String nombre;
    private Double precio;

    public ProductoDTO(String nombre, Double precio, String categoriaNombre) {
        this.nombre = nombre;
        this.precio = precio;      
    }

    public String getNombre() {
        return nombre;
    }

    public Double getPrecio() {
        return precio;
    }
}
