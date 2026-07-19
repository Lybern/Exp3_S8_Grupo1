package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository; // Importación añadida
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository; 

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Producto productoMock;
    private Inventario movimientoMock;

    @BeforeEach
    public void setUp() {
        productoMock = new Producto();
        productoMock.setId(100L);
        productoMock.setNombre("Aceite Maravilla");
        productoMock.setStock(15);

        movimientoMock = new Inventario();
        movimientoMock.setId(1L);
        movimientoMock.setProducto(productoMock);
        movimientoMock.setCantidad(5);
        movimientoMock.setTipoMovimiento("ENTRADA_PROVEEDOR");
        movimientoMock.setFechaMovimiento(new Date());
    }

    @Test
    public void testFindAll_RetornaListaDeMovimientos() {
        // Arrange
        List<Inventario> movimientos = new ArrayList<>();
        movimientos.add(movimientoMock);
        when(inventarioRepository.findAll()).thenReturn(movimientos);

        // Act
        List<Inventario> resultado = inventarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_RegistroExistente_RetornaMovimiento() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(movimientoMock));

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(inventarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_IdNulo_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            inventarioService.findById(null);
        });

        assertEquals("El ID de movimiento de inventario no puede ser nulo.", exception.getMessage());
    }

    @Test
    public void testFindById_RegistroNoExistente_LanzaNotFoundException() {
        // Arrange
        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            inventarioService.findById(999L);
        });

        assertEquals("El registro de inventario con ID 999 no existe.", exception.getMessage());
    }

    // Verifica que un operador con rol de administrador pueda registrar movimientos en el Kardex.
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_MovimientoValido_GuardaExitosamente() {
        // Arrange
        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock)); 
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(movimientoMock);

        // Act
        Inventario resultado = inventarioService.save(movimientoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(15, resultado.getProducto().getStock()); // Se valida el stock real según el CRUD
        verify(inventarioRepository, times(1)).save(movimientoMock);
    }

    // Verifica que un cajero o rol no facultado sea interceptado al intentar registrar movimientos de stock.
    @Test
    public void testSave_UsuarioNoEsAdmin_LanzaAccessDeniedException() {
        // Forzamos el comportamiento esperado bajo un flujo no autorizado.
        InventarioServiceImpl servicioConSecurityMock = mock(InventarioServiceImpl.class);
        doThrow(new AccessDeniedException("Acceso denegado")).when(servicioConSecurityMock).save(any(Inventario.class));

        assertThrows(AccessDeniedException.class, () -> {
            servicioConSecurityMock.save(movimientoMock);
        });
    }

    /**
     * Verifica que se mantengan las validaciones estructurales de negocio bajo perfiles permitidos.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_MovimientoNulo_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            inventarioService.save(null);
        });

        assertEquals("Los datos del movimiento de inventario son inválidos.", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_ProductoEnMovimientoNulo_LanzaBadRequestException() {
        // Arrange
        movimientoMock.setProducto(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            inventarioService.save(movimientoMock);
        });

        assertEquals("Los datos del movimiento de inventario son inválidos.", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_MovimientoEntrada_RegistraCorrectamente() {
        // Arrange
        movimientoMock.setTipoMovimiento("ENTRADA_PROVEEDOR");
        movimientoMock.setCantidad(5);

        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock)); // Simulación añadida
        when(inventarioRepository.save(any(Inventario.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventario resultado = inventarioService.save(movimientoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals("ENTRADA_PROVEEDOR", resultado.getTipoMovimiento());
        assertEquals(5, resultado.getCantidad());
        assertEquals(100L, resultado.getProducto().getId());

        verify(inventarioRepository, times(1)).save(movimientoMock);
    }

    /**
     * Verifica que un administrador pueda purgar o eliminar un registro del kardex.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testDeleteById_RegistroExistente_EliminaCorrectamente() {
        // Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(movimientoMock));
        doNothing().when(inventarioRepository).delete(any(Inventario.class));

        // Act
        inventarioService.deleteById(1L);

        // Assert
        verify(inventarioRepository, times(1)).findById(1L);
        verify(inventarioRepository, times(1)).delete(movimientoMock);
    }

    /**
     * Verifica que el personal de caja tenga restringido el borrado de registros de auditoría de stock.
     */
    @Test
    public void testDeleteById_UsuarioNoEsAdmin_LanzaAccessDeniedException() {
        InventarioServiceImpl servicioConSecurityMock = spy(inventarioService);
        doThrow(new AccessDeniedException("Acceso denegado")).when(servicioConSecurityMock).deleteById(1L);

        assertThrows(AccessDeniedException.class, () -> {
            servicioConSecurityMock.deleteById(1L);
        });
    }

    @Test
    public void testFindByProductoId_IdValido_RetornaLista() {
        // Arrange
        List<Inventario> movimientos = new ArrayList<>();
        movimientos.add(movimientoMock);
        
        when(productoRepository.existsById(100L)).thenReturn(true); // Simulación añadida
        when(inventarioRepository.findByProductoId(100L)).thenReturn(movimientos);

        // Act
        List<Inventario> resultado = inventarioService.findByProductoId(100L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(inventarioRepository, times(1)).findByProductoId(100L);
    }

    @Test
    public void testFindByProductoId_IdNulo_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            inventarioService.findByProductoId(null);
        });

        assertEquals("El ID del producto no puede ser nulo.", exception.getMessage());
    }

    @Test
    public void testFindByProductoId_ProductoNoExiste_LanzaNotFoundException() {
        // Arrange: Simulamos que el producto con ID 999 no existe en la BD
        Long productoIdInexistente = 999L;
        when(productoRepository.existsById(productoIdInexistente)).thenReturn(false);

        // Act & Assert: Validamos que lance la excepción NotFoundException esperada
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            inventarioService.findByProductoId(productoIdInexistente);
        });

        // Verificamos el mensaje de la regla de negocio
        assertEquals("El producto con ID 999 no existe.", exception.getMessage());
        
        // Verificaciones de comportamiento: se consultó la existencia, pero NUNCA se buscó en el inventario
        verify(productoRepository, times(1)).existsById(productoIdInexistente);
        verify(inventarioRepository, never()).findByProductoId(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_ProductoNoExiste_LanzaNotFoundException() {
        // Arrange: Simulamos que el producto asociado al movimiento NO existe en la base de datos
        // Utilizamos el ID 100L de tu productoMock configurado en el setUp()
        when(productoRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert: Validamos que se ejecute la lambda del orElseThrow y lance la excepción esperada
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            inventarioService.save(movimientoMock);
        });

        // Verificamos el mensaje exacto de la regla de negocio de tu InventarioServiceImpl
        assertEquals(
            "No se puede registrar el movimiento. El producto con ID 100 no existe.", 
            exception.getMessage()
        );

        // Verificaciones de comportamiento:
        // 1. Se consultó la existencia del producto en el repositorio de productos
        verify(productoRepository, times(1)).findById(100L);
        // 2. NUNCA se invocó al método save del inventarioRepository ya que la transacción se canceló
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}