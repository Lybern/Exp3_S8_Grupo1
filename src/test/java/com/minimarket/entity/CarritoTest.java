package com.minimarket.entity;

import com.minimarket.exception.BadRequestException; // Importación agregada
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    private Carrito carrito;
    private Producto producto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(10);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente01");
        usuario.setPassword("123456");
        
        // El carrito ahora conoce a su usuario desde el inicio
        carrito.setUsuario(usuario);
    }

    @Test
    void agregarProducto_debeAgregarCuandoHayStockSuficiente() {
        carrito.agregarProducto(producto, 4);

        // Validamos que se haya añadido 1 elemento a la lista intermedia
        assertEquals(1, carrito.getItems().size());
        
        // Rescatamos ese detalle para verificar sus valores internos
        DetalleCarrito detalle = carrito.getItems().get(0);
        assertEquals(producto, detalle.getProducto());
        assertEquals(4, detalle.getCantidad());
        assertEquals(carrito, detalle.getCarrito());
    }

    @Test
    void agregarProducto_debeIncrementarContadorSiElProductoYaExiste() {
        // Agregamos primero 2 unidades
        carrito.agregarProducto(producto, 2);
        // Agregamos luego 3 unidades del mismo producto
        carrito.agregarProducto(producto, 3);

        // No debe duplicar filas en la lista intermedia; debe seguir siendo 1 solo ítem
        assertEquals(1, carrito.getItems().size());
        
        // El contador (cantidad) debe haber acumulado las unidades (2 + 3 = 5)
        DetalleCarrito detalle = carrito.getItems().get(0);
        assertEquals(5, detalle.getCantidad());
    }

    // --- PRUEBAS DE EXCEPCIONES (VALIDACIONES) ---

    @Test
    void agregarProducto_noDebeAgregarCuandoProductoEsNulo() {
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> carrito.agregarProducto(null, 4)
        );

        assertEquals("El producto no puede ser nulo", excepcion.getMessage());
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void agregarProducto_noDebeAgregarCuandoCantidadEsNula() {
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> carrito.agregarProducto(producto, null)
        );

        // Mensaje ajustado: "La cantidad debe ser mayor que cero" o "La cantidad debe ser mayor a cero" 
        // según lo dejamos en tu entidad final
        assertTrue(excepcion.getMessage().contains("La cantidad debe ser mayor"));
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void agregarProducto_noDebeAgregarCuandoCantidadEsMenorOIgualACero() {
        // Probamos con 0
        IllegalArgumentException excepcionCero = assertThrows(
                IllegalArgumentException.class,
                () -> carrito.agregarProducto(producto, 0)
        );
        assertTrue(excepcionCero.getMessage().contains("La cantidad debe ser mayor"));

        // Probamos con un número negativo (-2)
        IllegalArgumentException excepcionNegativo = assertThrows(
                IllegalArgumentException.class,
                () -> carrito.agregarProducto(producto, -2)
        );
        assertTrue(excepcionNegativo.getMessage().contains("La cantidad debe ser mayor"));
        
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void agregarProducto_debeSaltarProductosDiferentesEnElBucleYAcumularElCorrecto() {
        // 1. Creamos un segundo producto diferente (por ejemplo, Fideos)
        Producto productoDiferente = new Producto();
        productoDiferente.setId(2L); // ID distinto al de Arroz (1L)
        productoDiferente.setNombre("Fideos");
        productoDiferente.setPrecio(1200.0);
        productoDiferente.setStock(5);

        // 2. Poblamos el carrito con ambos productos para generar volumen en la lista
        carrito.agregarProducto(productoDiferente, 2); // Queda en el índice 0
        carrito.agregarProducto(producto, 3);          // Queda en el índice 1

        // 3. Intentamos agregar más unidades del SEGUNDO producto (Arroz, ID 1L)
        // Al ejecutar esto, el bucle evaluará el primer ítem (Fideos, ID 2L).
        // Como 2L.equals(1L) es FALSO, se cubre la rama faltante y continúa el bucle.
        carrito.agregarProducto(producto, 2);

        // --- VALIDACIONES ---
        // Deben seguir existiendo únicamente 2 registros en la lista intermedia
        assertEquals(2, carrito.getItems().size());

        // El primer ítem (Fideos) debe permanecer intacto con sus 2 unidades
        assertEquals(2, carrito.getItems().get(0).getCantidad());

        // El segundo ítem (Arroz) debe haber acumulado las unidades correctamente (3 + 2 = 5)
        assertEquals(5, carrito.getItems().get(1).getCantidad());
    }

    @Test
    void agregarProducto_noDebeAgregarCuandoStockEsInsuficiente() {
        producto.setStock(3); // Stock menor a la cantidad solicitada (5)

        // Corregido: Ahora espera BadRequestException en lugar de IllegalArgumentException
        BadRequestException excepcion = assertThrows(
                BadRequestException.class,
                () -> carrito.agregarProducto(producto, 5)
        );

        assertTrue(excepcion.getMessage().contains("Stock insuficiente"));
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void agregarProducto_noDebeAgregarCuandoStockEsInsuficienteAlAcumular() {
        producto.setStock(6);
        
        // Agregamos 4 unidades primero (Pasa bien)
        carrito.agregarProducto(producto, 4);

        // Intentamos agregar 3 unidades más (Total acumulado sería 7, supera el stock de 6)
        // Corregido: Ahora espera BadRequestException en lugar de IllegalArgumentException
        BadRequestException excepcion = assertThrows(
                BadRequestException.class,
                () -> carrito.agregarProducto(producto, 3)
        );

        assertTrue(excepcion.getMessage().contains("Stock insuficiente"));
        // El carrito debió quedarse congelado en las 4 unidades iniciales
        assertEquals(4, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_noDebeAgregarCuandoStockEsNulo() {
        producto.setStock(null);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> carrito.agregarProducto(producto, 2)
        );

        assertEquals("El producto no tiene stock definido", excepcion.getMessage());
        assertTrue(carrito.getItems().isEmpty());
    }

    // --- PRUEBAS DE ASOCIACIÓN DE INSTANCIAS ---

    @Test
    void carrito_debeMantenerUsuarioAsociado() {
        assertNotNull(carrito.getUsuario());
        assertSame(usuario, carrito.getUsuario());
        assertEquals(1L, carrito.getUsuario().getId());
    }

    @Test
    void agregarProducto_debeAsociarProductoCorrectoEnDetalle() {
        producto.setId(5L);
        producto.setNombre("Azúcar");

        carrito.agregarProducto(producto, 3);

        DetalleCarrito detalle = carrito.getItems().get(0);
        assertSame(producto, detalle.getProducto());
        assertEquals(5L, detalle.getProducto().getId());
        assertEquals("Azúcar", detalle.getProducto().getNombre());
    }
}