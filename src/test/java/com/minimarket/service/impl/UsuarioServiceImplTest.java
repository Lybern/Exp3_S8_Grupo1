package com.minimarket.service.impl;

import com.minimarket.entity.Usuario;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioMock;

    @BeforeEach
    public void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("admin.market");
        usuarioMock.setPassword("SecuredPass123!");
    }

    @Test
    public void testFindByUsername_UsuarioExiste_RetornaOptionalConUsuario() {
        // Arrange
        when(usuarioRepository.findByUsername("admin.market")).thenReturn(Optional.of(usuarioMock));

        // Act
        Optional<Usuario> resultado = usuarioService.findByUsername("admin.market");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("admin.market", resultado.get().getUsername());
        verify(usuarioRepository, times(1)).findByUsername("admin.market");
    }

    @Test
    public void testSave_UsuarioNuevo_GuardaExitosamente() {
        // Arrange
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // Act
        Usuario resultado = usuarioService.save(usuarioMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    public void testSave_UsuarioNulo_LanzaBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            usuarioService.save(null);
        });
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testFindAll_RetornaListaDeUsuarios() {
        // Arrange
        List<Usuario> listaMock = List.of(usuarioMock);
        when(usuarioRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Usuario> resultado = usuarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_UsuarioExiste_RetornaOptionalConUsuario() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        // Act
        Optional<Usuario> resultado = usuarioService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_IdNull_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            usuarioService.findById(null);
        });

        assertEquals("El ID de usuario no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testFindByUsername_UsernameVacio_LanzaBadRequestException() {
        // Act & Assert para Null
        BadRequestException exNull = assertThrows(BadRequestException.class, () -> {
            usuarioService.findByUsername(null);
        });
        assertEquals("El nombre de usuario no puede estar vacío.", exNull.getMessage());

        // Act & Assert para Vacío/Espacios
        BadRequestException exVacio = assertThrows(BadRequestException.class, () -> {
            usuarioService.findByUsername("   ");
        });
        assertEquals("El nombre de usuario no puede estar vacío.", exVacio.getMessage());
        
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testDeleteById_UsuarioExiste_EliminaCorrectamente() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        // Act
        usuarioService.deleteById(1L);

        // Assert
        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteById_IdNull_LanzaBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            usuarioService.deleteById(null);
        });

        assertEquals("El ID de usuario no puede ser nulo.", exception.getMessage());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testDeleteById_UsuarioNoExiste_LanzaNotFoundException() {
        // Arrange
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            usuarioService.deleteById(999L);
        });

        assertEquals("El usuario con ID 999 no existe.", exception.getMessage());
        verify(usuarioRepository, times(1)).existsById(999L);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}