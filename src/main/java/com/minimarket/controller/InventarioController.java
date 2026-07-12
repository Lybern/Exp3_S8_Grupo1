package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import com.minimarket.dto.InventarioRequestDTO;
import com.minimarket.dto.InventarioResponseDTO;
import com.minimarket.dto.ErrorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
@Tag(name = "Inventario", description = "API para gestionar el inventario del minimarket")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Error de validaciÃ³n o solicitud incorrecta", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
})
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    @Operation(summary = "Obtener movimientos de inventario", description = "Devuelve una lista de todos los movimientos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(mediaType = "application/json"))
    })
    @SecurityRequirement(name = "bearerAuth")
    public CollectionModel<EntityModel<InventarioResponseDTO>> listarMovimientos() {
        List<EntityModel<InventarioResponseDTO>> inventarios = inventarioService.findAll().stream()
                .map(inv -> EntityModel.of(InventarioResponseDTO.from(inv),
                        linkTo(methodOn(InventarioController.class).listarPorProducto(inv.getProducto().getId())).withRel("por-producto"),
                        linkTo(methodOn(InventarioController.class).listarMovimientos()).withRel("inventarios")))
                .collect(Collectors.toList());
        return CollectionModel.of(inventarios, linkTo(methodOn(InventarioController.class).listarMovimientos()).withSelfRel());
    }

    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener movimientos por producto", description = "Devuelve los movimientos de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimientos obtenidos exitosamente", content = @Content(mediaType = "application/json"))
    })
    @SecurityRequirement(name = "bearerAuth")
    public CollectionModel<EntityModel<InventarioResponseDTO>> listarPorProducto(@Parameter(description = "ID del producto") @PathVariable Long productoId) {
        List<EntityModel<InventarioResponseDTO>> inventarios = inventarioService.findByProductoId(productoId).stream()
                .map(inv -> EntityModel.of(InventarioResponseDTO.from(inv),
                        linkTo(methodOn(InventarioController.class).listarPorProducto(productoId)).withSelfRel(),
                        linkTo(methodOn(InventarioController.class).listarMovimientos()).withRel("inventarios")))
                .collect(Collectors.toList());
        return CollectionModel.of(inventarios, linkTo(methodOn(InventarioController.class).listarPorProducto(productoId)).withSelfRel());
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento", description = "Registra una entrada de mercadería o auditoría")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioResponseDTO.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EntityModel<InventarioResponseDTO>> registrarMovimiento(@Valid @RequestBody InventarioRequestDTO dto) {
        Inventario guardado = inventarioService.save(dto.toEntity());
        EntityModel<InventarioResponseDTO> model = EntityModel.of(InventarioResponseDTO.from(guardado),
                linkTo(methodOn(InventarioController.class).listarPorProducto(guardado.getProducto().getId())).withRel("por-producto"),
                linkTo(methodOn(InventarioController.class).listarMovimientos()).withRel("inventarios"));
        return ResponseEntity.status(201).body(model);
    }
}