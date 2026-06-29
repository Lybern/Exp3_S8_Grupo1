package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.DetalleCarrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Usuario usuarioMock;
    private Carrito carritoMock;
    private Producto productoMock;

    @BeforeEach
    public void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("juan.perez");

        carritoMock = new Carrito();
        carritoMock.setId(10L);
        carritoMock.setUsuario(usuarioMock);

        productoMock = new Producto();
        productoMock.setId(100L);
        productoMock.setNombre("Fideos");
        productoMock.setStock(10);
        productoMock.setPrecio(990.0);
    }

    @Test
    public void testObtenerOCrearCarrito_ExisteCarrito_RetornaExistente() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock));

        // Act
        Carrito resultado = carritoService.obtenerOCrearCarrito(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testObtenerOCrearCarrito_NoExisteCarrito_CreaYRetornaNuevo() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoMock);

        // Act
        Carrito resultado = carritoService.obtenerOCrearCarrito(1L);

        // Assert
        assertNotNull(resultado);
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
        verify(usuarioRepository, times(1)).findById(1L);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    public void testObtenerOCrearCarrito_UsuarioNoEncontrado_LanzaNotFoundException() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            carritoService.obtenerOCrearCarrito(1L);
        });
    }

    @Test
    public void testAgregarProductoAlCarrito_CaminoFeliz() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock));
        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoMock);

        // Act
        Carrito resultado = carritoService.agregarProductoAlCarrito(1L, 100L, 3);

        // Assert
        assertNotNull(resultado);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    public void testObtenerOCrearCarrito_UsuarioIdNull_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            carritoService.obtenerOCrearCarrito(null);
        });

        assertEquals("El ID de usuario no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(carritoRepository, usuarioRepository);
    }

    @Test
    public void testAgregarProductoAlCarrito_ParametrosInvalidos_LanzaBadRequestException() {
        // Caso 1: productoId nulo
        assertThrows(BadRequestException.class, () -> {
            carritoService.agregarProductoAlCarrito(1L, null, 5);
        });

        // Caso 2: cantidad nula
        assertThrows(BadRequestException.class, () -> {
            carritoService.agregarProductoAlCarrito(1L, 100L, null);
        });

        // Caso 3: cantidad menor o igual a cero
        assertThrows(BadRequestException.class, () -> {
            carritoService.agregarProductoAlCarrito(1L, 100L, 0);
        });

        verifyNoInteractions(productoRepository);
    }

    @Test
    public void testAgregarProductoAlCarrito_ProductoNoExiste_LanzaNotFoundException() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            carritoService.agregarProductoAlCarrito(1L, 999L, 2);
        });

        assertEquals("El producto con ID 999 no existe.", exception.getMessage());
    }

    @Test
    public void testAgregarProductoAlCarrito_StockInsuficiente_LanzaIllegalArgumentException() {
        // Arrange
        productoMock.setStock(2); // Solicitaremos más de lo disponible
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock));
        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            carritoService.agregarProductoAlCarrito(1L, 100L, 5);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
    }

    @Test
    public void testEliminarItemDelCarrito_CaminoFeliz() {
        // Arrange
        DetalleCarrito item = new DetalleCarrito(carritoMock, productoMock, 2);
        item.setId(55L);
        carritoMock.getItems().add(item);

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoMock);

        // Act
        Carrito resultado = carritoService.eliminarItemDelCarrito(1L, 55L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getItems().isEmpty());
        verify(carritoRepository, times(1)).save(carritoMock);
    }

    @Test
    public void testEliminarItemDelCarrito_DetalleIdNull_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            carritoService.eliminarItemDelCarrito(1L, null);
        });

        assertEquals("El ID de detalle no puede ser nulo.", exception.getMessage());
    }

    @Test
    public void testEliminarItemDelCarrito_ItemNoPerteneceAlCarrito_LanzaNotFoundException() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock)); // Carrito vacío sin ID 99L

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            carritoService.eliminarItemDelCarrito(1L, 99L);
        });

        assertEquals("El ítem con ID 99 no pertenece al carrito del usuario.", exception.getMessage());
    }

    @Test
    public void testVaciarCarrito_LimpiaItemsCorrectamente() {
        // Arrange
        DetalleCarrito item = new DetalleCarrito(carritoMock, productoMock, 2);
        carritoMock.getItems().add(item);

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carritoMock));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoMock);

        // Act
        carritoService.vaciarCarrito(1L);

        // Assert
        assertTrue(carritoMock.getItems().isEmpty());
        verify(carritoRepository, times(1)).save(carritoMock);
    }
}