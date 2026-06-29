package com.minimarket.security.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;


@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            usuarioRepository.deleteAll();
            rolRepository.deleteAll();

            Rol administradorRole = new Rol();
            administradorRole.setNombre("ROLE_ADMINISTRADOR");

            Rol cajeroRole = new Rol();
            cajeroRole.setNombre("ROLE_CAJERO");

            Rol clienteRole = new Rol();
            clienteRole.setNombre("ROLE_CLIENTE");

            rolRepository.save(administradorRole);
            rolRepository.save(cajeroRole);
            rolRepository.save(clienteRole);

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setRoles(Set.of(administradorRole));

            Usuario empleado = new Usuario();
            empleado.setUsername("cajero");
            empleado.setPassword(passwordEncoder.encode("1234"));
            empleado.setRoles(Set.of(cajeroRole));

            Usuario cliente = new Usuario();
            cliente.setUsername("cliente");
            cliente.setPassword(passwordEncoder.encode("1234"));
            cliente.setRoles(Set.of(clienteRole));

            usuarioRepository.save(admin);
            usuarioRepository.save(empleado);
            usuarioRepository.save(cliente);
        };
    }
}