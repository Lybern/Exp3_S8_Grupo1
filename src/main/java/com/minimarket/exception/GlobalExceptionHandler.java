package com.minimarket.exception;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.minimarket.dto.ErrorResponseDTO; // <-- IMPORTANTE: Importamos tu DTO real
import com.minimarket.security.monitor.SuspiciousActivityService;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private SuspiciousActivityService suspiciousActivityService;

    // 1. Recursos no encontrados (404)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(NotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                System.currentTimeMillis() // Pasamos el long en milisegundos que pide el DTO
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 2. Peticiones incorrectas (400)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(BadRequestException ex) {
        log.warn("Petición incorrecta (Bad Request): {}", ex.getMessage());
        
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /* 
    // 3. Fallos de login (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, WebRequest req) {
        log.warn("Authentication failed: {}", ex.getMessage());
        
        if (req instanceof ServletWebRequest) {
            HttpServletRequest httpReq = ((ServletWebRequest) req).getRequest();
            suspiciousActivityService.recordFailedLogin(httpReq, null);
        }

        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Username o contraseña incorrectos.",
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
    */

    // 3.1. Fallos de autorización y credenciales personalizadas (401)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Acceso no autorizado: {}", ex.getMessage());
        
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(), // Devuelve 401
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 4. Cuentas bloqueadas (423)
    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountBlocked(AccountBlockedException ex) {
        log.warn("Account blocked: {}", ex.getMessage());
        
        ErrorResponseDTO body = new ErrorResponseDTO(
                423, // Locked
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(423).body(body);
    }

    // 5. Accesos denegados por falta de roles (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado (403): Intento de acceso sin permisos.");
        
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "No tiene los permisos suficientes para acceder a este recurso.",
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 6. Salvavidas final para errores del servidor (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex) {
        log.error("Error interno del servidor no controlado: ", ex);
        
        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ha ocurrido un error interno en el servidor.",
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 7. Errores de validación de campos (@Valid) (400)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        // Juntamos todos los errores de campos si es que hay varios
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        if (errorMessage.isEmpty()) {
            errorMessage = "Error de validación en la solicitud.";
        }

        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Error de tipo de argumento en la URL: {}", ex.getMessage());
        
        // Creamos un mensaje amigable para el cliente
        String mensajeError = String.format(
            "El parámetro '%s' con el valor '%s' no pudo ser convertido al tipo requerido (%s).",
            ex.getName(), 
            ex.getValue(), 
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido"
        );

        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                mensajeError,
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}