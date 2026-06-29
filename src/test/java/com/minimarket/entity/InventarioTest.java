package com.minimarket.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void movimientoDebeSerValidoCuandoCamposEstanCompletos() {

        Producto producto = crearProducto();

        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> errores =
                validator.validate(inventario);

        assertTrue(errores.isEmpty());
    }

    @Test
    void tipoMovimientoNoDebeSerNulo() {

        Inventario inventario = new Inventario();
        inventario.setProducto(crearProducto());
        inventario.setCantidad(10);
        inventario.setTipoMovimiento(null);
        inventario.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> errores =
                validator.validate(inventario);

        assertFalse(errores.isEmpty());

        assertTrue(
                errores.stream().anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("tipoMovimiento")
                )
        );
    }

    @Test
    void tipoMovimientoNoDebeEstarVacio() {

        Inventario inventario = new Inventario();
        inventario.setProducto(crearProducto());
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("");
        inventario.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> errores =
                validator.validate(inventario);

        assertFalse(errores.isEmpty());

        assertTrue(
                errores.stream().anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("tipoMovimiento")
                )
        );
    }

    @Test
    void cantidadNoDebeSerNula() {

        Inventario inventario = new Inventario();
        inventario.setProducto(crearProducto());
        inventario.setCantidad(null);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> errores =
                validator.validate(inventario);

        assertFalse(errores.isEmpty());

        assertTrue(
                errores.stream().anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("cantidad")
                )
        );
    }

    @Test
    void cantidadDebeSerMayorQueCero() {

        Inventario inventario = new Inventario();
        inventario.setProducto(crearProducto());
        inventario.setCantidad(0);
        inventario.setTipoMovimiento("Salida");
        inventario.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> errores =
                validator.validate(inventario);

        assertFalse(errores.isEmpty());

        assertTrue(
                errores.stream().anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals("cantidad")
                )
        );
    }

    @Test
    void inventarioDebeMantenerProductoCorrecto() {

        Producto productoEsperado = crearProducto();

        Inventario inventario = new Inventario();
        inventario.setProducto(productoEsperado);
        inventario.setCantidad(5);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        assertNotNull(inventario.getProducto());

        assertSame(
                productoEsperado,
                inventario.getProducto()
        );

        assertEquals(
                1L,
                inventario.getProducto().getId()
        );

        assertEquals(
                "Arroz",
                inventario.getProducto().getNombre()
        );
    }

    private Producto crearProducto() {

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(20);

        return producto;
    }
}