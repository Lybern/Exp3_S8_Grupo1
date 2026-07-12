package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modelo estandarizado para respuestas de error de la API")
public class ErrorResponseDTO {

    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Mensaje descriptivo del error", example = "El recurso solicitado no fue encontrado en el sistema.")
    private String message;

    @Schema(description = "Timestamp del error en milisegundos", example = "1719827364812")
    private long timestamp;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(int status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
