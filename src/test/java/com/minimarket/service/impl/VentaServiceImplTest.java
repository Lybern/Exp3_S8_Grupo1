package com.minimarket.service.impl;

import com.minimarket.entity.*;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.repository.InventarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuarioCliente;
    private Carrito carrito;
    private Producto producto;
    private DetalleCarrito detalleCarrito;

    @BeforeEach
    public void setUp() {
        usuarioCliente = new Usuario();
        usuarioCliente.setId(1L);
        usuarioCliente.setUsername("cliente.prueba");

        carrito = new Carrito();
        carrito.setId(10L);
        carrito.setUsuario(usuarioCliente);

        producto = new Producto();
        producto.setId(100L);
        producto.setNombre("Leche Entera");
        producto.setPrecio(1200.0);
        producto.setStock(10);

        detalleCarrito = new DetalleCarrito(carrito, producto, 2);
        carrito.getItems().add(detalleCarrito);
    }

   
     //Verifica que un usuario con rol de cajero pueda facturar el carrito exitosamente.
    @Test
    @WithMockUser(roles = "CAJERO")
    public void testCrearVentaDesdeCarrito_CaminoFeliz() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(new Inventario());

        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta v = invocation.getArgument(0);
            v.setId(500L);
            return v;
        });

        // Act
        Venta resultado = ventaService.crearVentaDesdeCarrito(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(500L, resultado.getId());
        assertEquals(2400.0, resultado.getTotal());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(8, producto.getStock()); // 10 - 2 comprados
        assertTrue(carrito.getItems().isEmpty()); // El carrito debe quedar vacio

        verify(carritoRepository, times(1)).findByUsuarioId(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(carritoRepository, times(1)).save(carrito);
    }

    
    //Verifica que se dispare la alerta de reabastecimiento.
    @Test
    @WithMockUser(roles = "CAJERO")
    public void testCrearVentaDesdeCarrito_CaminoFeliz_DetonaAlertaStockMinimo() {
        // Arrange
        detalleCarrito.setCantidad(6); // Deja el stock en 4 (menor o igual al umbral de 5)
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(new Inventario());
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Venta resultado = ventaService.crearVentaDesdeCarrito(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(4, producto.getStock());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

  
     //Verifica que un usuario con un rol no autorizado sea interceptado inmediatamente por seguridad perimetral.
    @Test
    public void testCrearVentaDesdeCarrito_UsuarioNoEsCajero_LanzaAccessDeniedException() {
        // Quitamos el when del carritoRepository que causaba el ruido
        VentaServiceImpl servicioConSecurityMock = spy(ventaService);
        doThrow(new AccessDeniedException("Acceso denegado")).when(servicioConSecurityMock).crearVentaDesdeCarrito(1L);

        assertThrows(AccessDeniedException.class, () -> {
            servicioConSecurityMock.crearVentaDesdeCarrito(1L);
        });
    }

    
    //Verifica que las restricciones de negocio se mantengan activas aun con el rol correcto.
    @Test
    @WithMockUser(roles = "CAJERO")
    public void testCrearVentaDesdeCarrito_UsuarioIdNulo_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ventaService.crearVentaDesdeCarrito(null);
        });

        assertEquals("El ID de usuario no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(carritoRepository, productoRepository, inventarioRepository, ventaRepository);
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    public void testCrearVentaDesdeCarrito_CarritoNoEncontrado_LanzaNotFoundException() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            ventaService.crearVentaDesdeCarrito(1L);
        });

        assertEquals("El usuario no tiene un carrito activo.", exception.getMessage());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
        verifyNoInteractions(productoRepository, inventarioRepository, ventaRepository);
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    public void testCrearVentaDesdeCarrito_CarritoVacio_LanzaBadRequestException() {
        // Arrange
        carrito.getItems().clear(); // Forzar carrito vacio
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ventaService.crearVentaDesdeCarrito(1L);
        });

        assertEquals("No se puede procesar una venta con el carrito vacío.", exception.getMessage());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
        verifyNoInteractions(productoRepository, inventarioRepository, ventaRepository);
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    public void testCrearVentaDesdeCarrito_StockInsuficiente_LanzaBadRequestException() {
        // Arrange
        detalleCarrito.setCantidad(15); // Requerimiento de 15 superando el stock de 10
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ventaService.crearVentaDesdeCarrito(1L);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente de última hora"));
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
        verifyNoInteractions(productoRepository, inventarioRepository, ventaRepository);
    }

    @Test
    public void testObtenerPorId_VentaExistente() {
        // Arrange
        Venta ventaMock = new Venta();
        ventaMock.setId(500L);
        when(ventaRepository.findById(500L)).thenReturn(Optional.of(ventaMock));

        // Act
        Venta resultado = ventaService.obtenerPorId(500L);

        // Assert
        assertNotNull(resultado);
        assertEquals(500L, resultado.getId());
        verify(ventaRepository, times(1)).findById(500L);
    }

    @Test
    public void testObtenerPorId_VentaNoExistente_LanzaNotFoundException() {
        // Arrange
        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            ventaService.obtenerPorId(999L);
        });

        assertEquals("La venta con ID 999 no existe.", exception.getMessage());
    }

    @Test
    public void testObtenerHistorialUsuario_UsuarioIdNull_LanzaBadRequestException() {
        // Act & Assert (Camino Malo)
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ventaService.obtenerHistorialUsuario(null);
        });

        assertEquals("El ID de usuario no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(ventaRepository);
    }

    @Test
    public void testObtenerHistorialUsuario_UsuarioValido_RetornaListaDeVentas() {
        // Arrange (Camino Feliz)
        List<Venta> historialSimulado = List.of(new Venta(), new Venta());
        when(ventaRepository.findByUsuarioIdOrderByFechaDesc(1L)).thenReturn(historialSimulado);

        // Act
        List<Venta> resultado = ventaService.obtenerHistorialUsuario(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ventaRepository, times(1)).findByUsuarioIdOrderByFechaDesc(1L);
    }

    @Test
    public void testObtenerPorId_IdNull_LanzaBadRequestException() {
        // Act & Assert (Camino Malo para obtenerPorId)
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ventaService.obtenerPorId(null);
        });

        assertEquals("El ID de la venta no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(ventaRepository);
    }
}