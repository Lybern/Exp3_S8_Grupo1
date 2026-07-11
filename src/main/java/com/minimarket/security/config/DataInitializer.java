package com.minimarket.security.config;

import java.util.Set;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // --- 1. LIMPIEZA DE TABLAS (En orden de dependencia para evitar violaciones de clave foránea) ---
            usuarioRepository.deleteAll();
            rolRepository.deleteAll();
            productoRepository.deleteAll();
            categoriaRepository.deleteAll();

            // --- 2. INICIALIZACIÓN DE ROLES Y USUARIOS ---
            Rol administradorRole = new Rol();
            administradorRole.setNombre("ROLE_ADMINISTRADOR");

            Rol cajeroRole = new Rol();
            cajeroRole.setNombre("ROLE_CAJERO");

            Rol clienteRole = new Rol();
            clienteRole.setNombre("ROLE_CLIENTE");

            rolRepository.saveAll(List.of(administradorRole, cajeroRole, clienteRole));

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

            usuarioRepository.saveAll(List.of(admin, empleado, cliente));

            // --- 3. INICIALIZACIÓN DE CATEGORÍAS ---
            Categoria abarrotes = new Categoria();
            abarrotes.setNombre("Abarrotes");

            Categoria bebidas = new Categoria();
            bebidas.setNombre("Bebidas y Lácteos");

            Categoria limpieza = new Categoria();
            limpieza.setNombre("Aseo y Limpieza");

            categoriaRepository.saveAll(List.of(abarrotes, bebidas, limpieza));

            // --- 4. INICIALIZACIÓN DE PRODUCTOS ---
            // Productos para Abarrotes
            Producto arroz = new Producto();
            arroz.setNombre("Arroz Grado 1 (1kg)");
            arroz.setPrecio(1300.0);
            arroz.setStock(50);
            arroz.setCategoria(abarrotes);

            Producto fideos = new Producto();
            fideos.setNombre("Fideos Spaghetti (400g)");
            fideos.setPrecio(850.0);
            fideos.setStock(100);
            fideos.setCategoria(abarrotes);

            // Productos para Bebidas y Lácteos
            Producto leche = new Producto();
            leche.setNombre("Leche Entera (1L)");
            leche.setPrecio(1100.0);
            leche.setStock(30);
            leche.setCategoria(bebidas);

            Producto bebidaCola = new Producto();
            bebidaCola.setNombre("Bebida Cola (2.5L)");
            bebidaCola.setPrecio(2200.0);
            bebidaCola.setStock(40);
            bebidaCola.setCategoria(bebidas);

            // Productos para Aseo y Limpieza
            Producto Detergente = new Producto();
            Detergente.setNombre("Detergente Líquido (3L)");
            Detergente.setPrecio(7500.0);
            Detergente.setStock(15);
            Detergente.setCategoria(limpieza);

            // Guardar todos los productos en lote
            productoRepository.saveAll(List.of(arroz, fideos, leche, bebidaCola, Detergente));
            
        };
    }
}