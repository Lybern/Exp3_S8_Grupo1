package com.minimarket.entity;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


public class ProductoTest {

    Producto producto = new Producto(); 

    @BeforeEach
    void setUP(){
        this.producto = producto;
    }
    @Test
    void testGetCategoria() {
        // 1. Arrange (Preparar el escenario)
        Categoria categoriaProducto = new Categoria(null); 
        categoriaProducto.setNombre("conservas");
        producto.setCategoria(categoriaProducto);

        // 2. Act (Ejecutar la acción)
        Categoria categoriaObtenida = producto.getCategoria();

        // 3. Assert (Verificar)
        assertNotNull(categoriaObtenida, "La categoría no debe ser nula");
        assertEquals("conservas", categoriaObtenida.getNombre(), "El nombre de la categoría debe ser 'conservas'");
    }




}
