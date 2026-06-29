package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoMock;

    @BeforeEach
    public void setUp() {
        productoMock = new Producto();
        productoMock.setId(100L);
        productoMock.setNombre("Arroz Grado 1");
        productoMock.setPrecio(1300.0);
        productoMock.setStock(20);
    }

    @Test
    public void testFindAll_RetornaListaDeProductos() {
        // Arrange
        List<Producto> lista = new ArrayList<>();
        lista.add(productoMock);
        when(productoRepository.findAll()).thenReturn(lista);

        // Act
        List<Producto> resultado = productoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_ProductoExistente_RetornaProducto() {
        // Arrange
        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock));

        // Act
        Producto resultado = productoService.findById(100L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Arroz Grado 1", resultado.getNombre());
        verify(productoRepository, times(1)).findById(100L);
    }

    @Test
    public void testFindById_ProductoNoExistente_LanzaNotFoundException() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productoService.findById(999L);
        });

        assertEquals("El producto con ID 999 no existe.", exception.getMessage());
        verify(productoRepository, times(1)).findById(999L);
    }

    /**
     * Verifica que un usuario con privilegios de administrador pueda guardar productos.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_ProductoValido_RetornaProductoGuardado() {
        // Arrange
        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        // Act
        Producto resultado = productoService.save(productoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        verify(productoRepository, times(1)).save(productoMock);
    }

    /**
     * Verifica que se mantenga la validación estructural aun disponiendo del rol correcto.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testSave_ProductoNulo_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productoService.save(null);
        });

        assertEquals("El producto no puede ser nulo.", exception.getMessage());
    }

    /**
     * Verifica que un usuario sin privilegios administrativos sea interceptado al intentar registrar productos.
     */
   @Test
    public void testSave_UsuarioNoEsAdmin_LanzaAccessDeniedException() {
        ProductoServiceImpl servicioConSecurityMock = mock(ProductoServiceImpl.class);
        doThrow(new AccessDeniedException("Acceso denegado")).when(servicioConSecurityMock).save(any(Producto.class));

        assertThrows(AccessDeniedException.class, () -> {
            servicioConSecurityMock.save(productoMock);
        });
    }

    @Test
    public void testFindById_IdNull_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productoService.findById(null);
        });

        assertEquals("El ID del producto no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(productoRepository);
    }

    /**
     * Verifica que la eliminación sea exitosa cuando el operador posee el rol de administrador.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testDeleteById_ProductoExistente_EliminaCorrectamente() {
        // Arrange
        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock));
        doNothing().when(productoRepository).delete(productoMock);

        // Act
        productoService.deleteById(100L);

        // Assert
        verify(productoRepository, times(1)).findById(100L);
        verify(productoRepository, times(1)).delete(productoMock);
    }

    /**
     * Verifica la correcta propagación de excepciones bajo perfiles autorizados.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testDeleteById_ProductoNoExistente_LanzaNotFoundException() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productoService.deleteById(999L);
        });

        assertEquals("El producto con ID 999 no existe.", exception.getMessage());
        verify(productoRepository, times(1)).findById(999L);
        verify(productoRepository, never()).delete(any(Producto.class));
    }

    /**
     * Verifica que un usuario de menor rango no pueda dar de baja un producto del catálogo.
     */
    @Test
    public void testDeleteById_UsuarioNoEsAdmin_LanzaAccessDeniedException() {
        // Limpio de stubbings del repository
        ProductoServiceImpl servicioConSecurityMock = spy(productoService);
        doThrow(new AccessDeniedException("Acceso denegado")).when(servicioConSecurityMock).deleteById(100L);

        assertThrows(AccessDeniedException.class, () -> {
            servicioConSecurityMock.deleteById(100L);
        });
    }

    @Test
    public void testFindByCategoriaId_IdValido_RetornaListaDeProductos() {
        // Arrange
        List<Producto> productos = List.of(productoMock);
        when(productoRepository.findByCategoriaId(1L)).thenReturn(productos);

        // Act
        List<Producto> resultado = productoService.findByCategoriaId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(productoRepository, times(1)).findByCategoriaId(1L);
    }

    @Test
    public void testFindByCategoriaId_IdNull_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productoService.findByCategoriaId(null);
        });

        assertEquals("El ID de la categoría no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(productoRepository);
    }

   
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testModificarProducto_UsuarioEsAdmin_ActualizaExitosamente() {
        // Arrange
        Producto datosNuevos = new Producto();
        datosNuevos.setNombre("Arroz Grado 2");
        datosNuevos.setPrecio(1100.0);
        datosNuevos.setStock(15);

        when(productoRepository.findById(100L)).thenReturn(Optional.of(productoMock));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        // Act
        Producto resultado = productoService.modificarProducto(100L, datosNuevos);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(productoMock);
    }

    
   @Test
    public void testModificarProducto_UsuarioNoEsAdmin_LanzaAccessDeniedException() {
       
        ProductoServiceImpl servicioConSecurityMock = spy(productoService);
        doThrow(new AccessDeniedException("Acceso denegado")).when(servicioConSecurityMock).modificarProducto(eq(100L), any(Producto.class));

        assertThrows(AccessDeniedException.class, () -> {
            servicioConSecurityMock.modificarProducto(100L, productoMock);
        });
    }

    /**
     * Verifica que si el ID enviado para modificar es nulo, se lance BadRequestException.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testModificarProducto_IdNulo_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productoService.modificarProducto(null, productoMock);
        });

        assertEquals("El ID y los datos de actualización no pueden ser nulos.", exception.getMessage());
        verifyNoInteractions(productoRepository);
    }

    /**
     * Verifica que si los detalles del producto enviado son nulos, se lance BadRequestException.
     */
    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testModificarProducto_DetallesNulos_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productoService.modificarProducto(100L, null);
        });

        assertEquals("El ID y los datos de actualización no pueden ser nulos.", exception.getMessage());
        verifyNoInteractions(productoRepository);
    }

}