package com.minimarket.exception;

import java.time.LocalDateTime;

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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.minimarket.security.monitor.SuspiciousActivityService;

import jakarta.servlet.http.HttpServletRequest;

// @RestControllerAdvice indica que esta clase interceptará las excepciones lanzadas por
// cualquier controlador en toda la aplicación y devolverá las respuestas directamente en formato JSON.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Instancia del logger para guardar un registro interno de los errores sin exponerlos al cliente.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private SuspiciousActivityService suspiciousActivityService;

    // Maneja errores de recursos no encontrados (Ej: cuando se busca un usuario que no existe). Devuelve un 404.
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest req) {
        String path = req != null ? req.getDescription(false) : "Desconocido";
        log.warn("Recurso no encontrado: {} - URI: {}", ex.getMessage(), path);
        ErrorResponse body = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Maneja errores por peticiones incorrectas (Ej: si el usuario envía datos erróneos). Devuelve un 400.
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest req) {
        String path = req != null ? req.getDescription(false) : "Desconocido";
        log.warn("Petición incorrecta (Bad Request): {} - URI: {}", ex.getMessage(), path);
        ErrorResponse body = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Intercepta fallos de autenticación (Ej: contraseña incorrecta al hacer login). Devuelve un 401.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest req) {
        log.warn("Authentication failed: {}", ex.getMessage());
        
        // Registra la actividad sospechosa si el intento provino de una petición HTTP
        if (req instanceof ServletWebRequest) {
            HttpServletRequest httpReq = ((ServletWebRequest) req).getRequest();
            suspiciousActivityService.recordFailedLogin(httpReq, null);
        }
        
        String path = req != null ? req.getDescription(false) : "Desconocido";

        ErrorResponse body = new ErrorResponse(
                "Invalid username or password",
                HttpStatus.UNAUTHORIZED.value(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // Maneja los bloqueos de seguridad cuando un usuario autenticado intenta acceder a un recurso sin los roles necesarios. Devuelve un 403.
    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountBlocked(AccountBlockedException ex, WebRequest req) {
        String path = req != null ? req.getDescription(false) : "Desconocido";
        log.warn("Account blocked: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(
                ex.getMessage(),
                423, // 423 Locked
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(423).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        String path = req != null ? req.getDescription(false) : "Desconocido";
        log.warn("Acceso denegado (403): Intento de acceso sin permisos a URI: {}", path);
        ErrorResponse body = new ErrorResponse(
                "No tiene los permisos suficientes para acceder a este recurso.",
                HttpStatus.FORBIDDEN.value(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // Un "salvavidas" final. Captura CUALQUIER otra excepción (Error 500) que no hayamos contemplado arriba.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest req) {
        String path = req != null ? req.getDescription(false) : "Desconocido";
        // Usamos log.error y le pasamos 'ex' para que los desarrolladores puedan ver todo el rastro (stack trace) del error en consola.
        log.error("Error interno del servidor no controlado en la URI: {}", path, ex);
        // Pero al cliente solo le devolvemos un mensaje genérico para no revelar detalles sensibles o de la base de datos.
        ErrorResponse body = new ErrorResponse(
                "Ha ocurrido un error interno en el servidor.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // Sobrescribe el método base de Spring Boot que salta cuando fallan las validaciones de las anotaciones (Ej: @Valid en los controladores).
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        // Guardamos el error en una variable local para evitar la advertencia de posible puntero nulo
        FieldError fieldError = ex.getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "Error de validación";
        String path = request != null ? request.getDescription(false) : "Desconocido";
        
        ErrorResponse body = new ErrorResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}