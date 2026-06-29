package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.DetalleCarrito;
import com.minimarket.entity.Producto;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.DetalleCarritoRepository;
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
public class DetalleCarritoServiceImplTest {

    @Mock
    private DetalleCarritoRepository detalleCarritoRepository;

    @InjectMocks
    private DetalleCarritoServiceImpl detalleCarritoService;

    private DetalleCarrito detalleMock;
    private Producto productoMock;

    @BeforeEach
    public void setUp() {
        productoMock = new Producto();
        productoMock.setId(100L);
        productoMock.setNombre("Aceite");
        productoMock.setPrecio(1500.0);
        productoMock.setStock(10);

        Carrito carrito = new Carrito();
        carrito.setId(10L);

        detalleMock = new DetalleCarrito(carrito, productoMock, 2);
        detalleMock.setId(50L);
    }

    // ========================================================
    // PRUEBAS: findById
    // ========================================================

    @Test
    public void testFindById_DetalleExiste_RetornaDetalle() {
        // Arrange
        when(detalleCarritoRepository.findById(50L)).thenReturn(Optional.of(detalleMock));

        // Act
        DetalleCarrito resultado = detalleCarritoService.findById(50L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getCantidad());
        verify(detalleCarritoRepository, times(1)).findById(50L);
    }

    @Test
    public void testFindById_IdNulo_LanzaBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            detalleCarritoService.findById(null);
        });
    }

    @Test
    public void testFindById_DetalleNoExiste_LanzaNotFoundException() {
        // Arrange
        when(detalleCarritoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            detalleCarritoService.findById(999L);
        });
    }

    // ========================================================
    // PRUEBAS: actualizarCantidad
    // ========================================================

    @Test
    public void testActualizarCantidad_CaminoFeliz() {
        // Arrange
        when(detalleCarritoRepository.findById(50L)).thenReturn(Optional.of(detalleMock));
        when(detalleCarritoRepository.save(any(DetalleCarrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DetalleCarrito resultado = detalleCarritoService.actualizarCantidad(50L, 5);

        // Assert
        assertNotNull(resultado);
        assertEquals(5, resultado.getCantidad());
        verify(detalleCarritoRepository, times(1)).save(any(DetalleCarrito.class));
    }

    @Test
    public void testActualizarCantidad_CantidadInvalida_LanzaBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            detalleCarritoService.actualizarCantidad(50L, 0);
        });
        
        assertThrows(BadRequestException.class, () -> {
            detalleCarritoService.actualizarCantidad(50L, -3);
        });

        //caso para null
        assertThrows(BadRequestException.class, () -> {
        detalleCarritoService.actualizarCantidad(50L, null);
        });
    }

    @Test
    public void testActualizarCantidad_StockInsuficiente_LanzaBadRequestException() {
        // Arrange
        when(detalleCarritoRepository.findById(50L)).thenReturn(Optional.of(detalleMock));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            detalleCarritoService.actualizarCantidad(50L, 15); // Supera las 10 unidades de stock disponibles
        });
    }

    @Test
    public void testActualizarCantidad_ProductoNoExiste_LanzaNotFoundException() {
        // Arrange
        detalleMock.setProducto(null); // Simulamos la desasociación del producto
        when(detalleCarritoRepository.findById(50L)).thenReturn(Optional.of(detalleMock));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            detalleCarritoService.actualizarCantidad(50L, 3);
        });
    }

    // ========================================================
    // PRUEBAS: eliminarItem
    // ========================================================

    @Test
    public void testEliminarItem_CaminoFeliz() {
        // Arrange
        when(detalleCarritoRepository.findById(50L)).thenReturn(Optional.of(detalleMock));
        doNothing().when(detalleCarritoRepository).delete(any(DetalleCarrito.class));

        // Act
        detalleCarritoService.eliminarItem(50L);

        // Assert
        verify(detalleCarritoRepository, times(1)).findById(50L);
        verify(detalleCarritoRepository, times(1)).delete(any(DetalleCarrito.class));
    }
}