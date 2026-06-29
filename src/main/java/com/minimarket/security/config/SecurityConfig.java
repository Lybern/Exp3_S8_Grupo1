package com.minimarket.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.minimarket.security.exception.CustomAccessDeniedHandler;
import com.minimarket.security.exception.JwtAuthenticationEntryPoint;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.service.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exc -> exc
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        )
        .authorizeHttpRequests(auth -> auth

                // 1. AUTENTICACION Y H2
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/registro-interno").hasRole("ADMINISTRADOR")
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/auth/login", "/logout").permitAll()

                // 2. GESTION DE USUARIOS
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")

                // 3. CONTROL DE PRODUCTOS (Requisito: Solo Admin modifica)
                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMINISTRADOR")

                // 4. CATEGORIAS e INVENTARIO
                .requestMatchers("/api/categorias/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                .requestMatchers("/api/inventario/**").hasRole("ADMINISTRADOR")

                // 5. CONTROL DE VENTAS (Requisito: Solo Cajero genera)
                .requestMatchers(HttpMethod.POST, "/api/ventas/checkout/**").hasRole("CAJERO")
                .requestMatchers(HttpMethod.GET, "/api/ventas/**").hasAnyRole("ADMINISTRADOR", "CAJERO")

                // 6. ELEMENTOS DEL CARRITO (Auditoria y Operacion compartida)
                .requestMatchers("/api/detalle-ventas/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                .requestMatchers("/api/carrito/**").hasAnyRole("CLIENTE", "ADMINISTRADOR", "CAJERO")

                // Regla general de cierre
                .anyRequest().authenticated()
        )
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/public/hola")
                .permitAll()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}