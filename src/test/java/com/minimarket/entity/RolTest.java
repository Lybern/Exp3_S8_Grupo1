package com.minimarket.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
// 👈 IMPORTANTE: Si la clase Usuario está en otro paquete o necesitas mapearla bien
// Asegúrate de que compile importando com.minimarket.entity.Usuario; si es necesario.

public class RolTest {

    @Test
    void testConstructorConParametros() {
        // 1. Arrange
        Long idEsperado = 1L;
        String nombreEsperado = "ADMIN";
        Set<Usuario> usuariosEsperados = new HashSet<>(); 
        
        // 2. Act (Requiere @AllArgsConstructor en la clase Rol)
        Rol rol = new Rol(idEsperado, nombreEsperado, usuariosEsperados);

        // 3. Assert
        assertNotNull(rol, "El objeto Rol no debería ser nulo");
        assertEquals(idEsperado, rol.getId(), "El ID no se asignó correctamente en el constructor");
        assertEquals(nombreEsperado, rol.getNombre(), "El nombre no se asignó correctamente en el constructor");
        assertEquals(usuariosEsperados, rol.getUsuarios(), "El Set de usuarios no se asignó correctamente");
    }
}