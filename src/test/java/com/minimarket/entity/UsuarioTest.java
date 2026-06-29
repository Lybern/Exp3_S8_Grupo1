package com.minimarket.entity;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void testGetRoles() {
        Usuario usuario = new Usuario();
        
        Rol rolAdmin = new Rol(null, "ADMIN", null);
        
        Set<Rol> roles = Set.of(rolAdmin);
        usuario.setRoles(roles);
        
        assertNotNull(usuario.getRoles());
        assertEquals(1, usuario.getRoles().size());
        assertTrue(usuario.getRoles().contains(rolAdmin));
    }
}