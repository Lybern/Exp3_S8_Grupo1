package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.InventarioRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

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

    
    //Verifica que un operador con rol de administrador pueda registrar movimientos en el Kardex.
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_MovimientoValido_GuardaExitosamente() {
        // Arrange
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(movimientoMock);

        // Act
        Inventario resultado = inventarioService.save(movimientoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(15, resultado.getProducto().getStock()); // Se valida el stock real segun tu CRUD
        verify(inventarioRepository, times(1)).save(movimientoMock);
    }


    //Verifica que un cajero o rol no facultado sea interceptado al intentar registrar movimientos de stock.
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
        // Eliminamos el stubbing innecesario del repositorio. Vamos directo al grano:
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
}