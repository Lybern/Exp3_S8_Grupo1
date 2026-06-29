package com.minimarket.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DetalleVentaTest {

    @Test
    @DisplayName("Debe instanciar DetalleVenta usando el constructor vacío")
    void testConstructorVacio() {
        // Provoca la ejecución del constructor por defecto (exigido por JPA)
        DetalleVenta detalle = new DetalleVenta();
        
        assertNotNull(detalle);
        assertNull(detalle.getId());
        assertNull(detalle.getVenta());
        assertNull(detalle.getProducto());
        assertNull(detalle.getCantidad());
        assertNull(detalle.getPrecio());
    }

    @Test
    @DisplayName("Debe asignar correctamente todos los campos usando el constructor con argumentos")
    void testConstructorConArgumentos() {
        // 1. Given: Preparamos las dependencias simuladas de la entidad
        Venta venta = new Venta();
        venta.setId(10L);

        Producto producto = new Producto();
        producto.setId(5L);
        producto.setNombre("Arroz");

        Integer cantidad = 3;
        Double precio = 1500.0;

        // 2. When: Invocamos el constructor que queremos llevar al 100%
        DetalleVenta detalle = new DetalleVenta(venta, producto, cantidad, precio);

        // 3. Then: Validamos que cada parámetro se haya mapeado correctamente a su atributo
        assertNotNull(detalle);
        assertSame(venta, detalle.getVenta(), "La venta asignada debe ser la misma instancia");
        assertSame(producto, detalle.getProducto(), "El producto asignado debe ser la misma instancia");
        assertEquals(3, detalle.getCantidad(), "La cantidad debe coincidir con el parámetro");
        assertEquals(1500.0, detalle.getPrecio(), "El precio debe coincidir con el parámetro");
    }
}
