package com.minimarket.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.minimarket.dto.ErrorResponseDTO;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minimarket.dto.JwtResponse;
import com.minimarket.dto.LoginRequest;
import com.minimarket.dto.PublicRegisterRequest;
import com.minimarket.dto.RegisterRequest;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.AccountBlockedException;
import com.minimarket.exception.BadRequestException;
import com.minimarket.exception.NotFoundException;
import com.minimarket.service.RolService; 
import com.minimarket.service.UsuarioService; 
import com.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.security.util.XssSanitizer;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Auth", description = "API de Autenticación")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Error de validación o solicitud incorrecta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
})
public class AuthController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SuspiciousActivityService suspiciousActivityService;

    public AuthController(
            UsuarioService usuarioService, 
            RolService rolService, 
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            SuspiciousActivityService suspiciousActivityService
    ) {
        this.usuarioService = usuarioService; 
        this.rolService = rolService; 
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.suspiciousActivityService = suspiciousActivityService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @io.swagger.v3.oas.annotations.Operation(summary="Registro interno")
    @ApiResponse(responseCode="200", description="Registrado", content=@Content(mediaType="application/json"))
    @PostMapping("/registro-interno")
    public ResponseEntity<?> registroInterno(@RequestBody RegisterRequest request) {

        // Validaciones básicas de nulidad
        if (request.getUsername() == null || request.getRol() == null) {
            throw new BadRequestException("El nombre de usuario y el rol son obligatorios.");
        }

        String safeUsername = XssSanitizer.sanitize(request.getUsername());

        if (usuarioService.findByUsername(safeUsername).isPresent()) {
            throw new BadRequestException("El usuario ya existe");
        }
        
        if (request.getPassword() == null || !request.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            throw new BadRequestException("La contraseña debe tener mínimo 8 caracteres, al menos un número, una mayúscula, una minúscula y un carácter especial.");
        }

    
        String nombreRolCompleto = "ROLE_" + request.getRol().toUpperCase().trim();
        
        
        Rol rol = rolService 
                .findByNombre(nombreRolCompleto)
                .orElseThrow(() -> new NotFoundException("El rol " + nombreRolCompleto + " no está dado de alta en el sistema."));

        // 4. Creación del usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(safeUsername);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRoles(Set.of(rol));

        usuarioService.save(usuario);

        return ResponseEntity.ok(
                Map.of(
                        "mensaje", "Usuario registrado internamente con éxito",
                        "username", usuario.getUsername(),
                        "rol", rol.getNombre() // Retorna el nombre oficial del rol asignado
                )
        );
    }


    // Endpoint para registro público (clientes), define el rol por defecto.
    @io.swagger.v3.oas.annotations.Operation(summary="Registro público")
    @ApiResponse(responseCode="200", description="Registrado", content=@Content(mediaType="application/json"))
    @PostMapping("/register")
    public ResponseEntity<?> publicRegister(@RequestBody PublicRegisterRequest request) {

        // Validación anti-NullPointer
        if (request.getUsername() == null) {
            throw new BadRequestException("El nombre de usuario es obligatorio.");
        }

        String safeUsername = XssSanitizer.sanitize(request.getUsername());

        if (usuarioService.findByUsername(safeUsername).isPresent()) { // Cambiado
            throw new BadRequestException("El usuario ya existe");
        }
        
        if (request.getPassword() == null || !request.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            throw new BadRequestException("La contraseña debe tener mínimo 8 caracteres, al menos un número, una mayúscula, una minúscula y un carácter especial.");
        }

        Rol rolCliente = rolService
                .findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new NotFoundException("Rol CLIENTE no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setUsername(safeUsername);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRoles(Set.of(rolCliente));

        usuarioService.save(usuario); 

        return ResponseEntity.ok(
                Map.of(
                        "mensaje", "Cliente registrado correctamente",
                        "username", usuario.getUsername(),
                        "rol", "CLIENTE"
                )
        );
    }


    // Endpoint para login, con protección contra ataques de fuerza bruta.
    @io.swagger.v3.oas.annotations.Operation(summary="Iniciar sesiÃ³n")
    @ApiResponse(responseCode="200", description="Login exitoso", content=@Content(mediaType="application/json"))
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        // Validación anti-NullPointer
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new BadRequestException("El usuario y la contraseña son obligatorios.");
        }

        if (suspiciousActivityService.isAccountBlocked(request, loginRequest.getUsername())) {
            throw new AccountBlockedException("Cuenta bloqueada temporalmente por demasiados intentos fallidos. Intente más tarde.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            suspiciousActivityService.recordFailedLogin(request, loginRequest.getUsername());
            throw new BadRequestException("Credenciales inválidas");
        }

        Usuario usuarioBD = usuarioService 
                .findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuarioBD);

        return ResponseEntity.ok(new JwtResponse(token));
    }
}