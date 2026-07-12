package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
@Tag(name = "Inventario", description = "API para gestionar el inventario del minimarket")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    @Operation(summary = "Obtener movimientos de inventario", description = "Devuelve una lista de todos los movimientos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @SecurityRequirement(name = "bearerAuth")
    public CollectionModel<EntityModel<Inventario>> listarMovimientos() {
        List<EntityModel<Inventario>> inventarios = inventarioService.findAll().stream()
                .map(inv -> EntityModel.of(inv,
                        linkTo(methodOn(InventarioController.class).listarPorProducto(inv.getProducto().getId())).withRel("por-producto"),
                        linkTo(methodOn(InventarioController.class).listarMovimientos()).withRel("inventarios")))
                .collect(Collectors.toList());
        return CollectionModel.of(inventarios, linkTo(methodOn(InventarioController.class).listarMovimientos()).withSelfRel());
    }

    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener movimientos por producto", description = "Devuelve los movimientos de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimientos obtenidos exitosamente")
    })
    @SecurityRequirement(name = "bearerAuth")
    public CollectionModel<EntityModel<Inventario>> listarPorProducto(@PathVariable Long productoId) {
        List<EntityModel<Inventario>> inventarios = inventarioService.findByProductoId(productoId).stream()
                .map(inv -> EntityModel.of(inv,
                        linkTo(methodOn(InventarioController.class).listarPorProducto(productoId)).withSelfRel(),
                        linkTo(methodOn(InventarioController.class).listarMovimientos()).withRel("inventarios")))
                .collect(Collectors.toList());
        return CollectionModel.of(inventarios, linkTo(methodOn(InventarioController.class).listarPorProducto(productoId)).withSelfRel());
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento", description = "Registra una entrada de mercadería o auditoría")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EntityModel<Inventario>> registrarMovimiento(@RequestBody Inventario inventario) {
        Inventario guardado = inventarioService.save(inventario);
        EntityModel<Inventario> model = EntityModel.of(guardado,
                linkTo(methodOn(InventarioController.class).listarPorProducto(guardado.getProducto().getId())).withRel("por-producto"),
                linkTo(methodOn(InventarioController.class).listarMovimientos()).withRel("inventarios"));
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(model);
    }
}