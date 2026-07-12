package com.minimarket.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.minimarket.dto.ErrorResponseDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Error de validaciÃ³n o solicitud incorrecta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
})
public class HolaMundoController {

    @GetMapping("/public/hola")
    public String holaMundo() {
        return "¡Hola Mundo!";
    }
}
