package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "API para gestionar el carrito de compras")
public class CarritoController {

  @Autowired
  private CarritoService carritoService;

  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
  @Operation(summary = "Obtener carrito por ID de usuario", description = "Devuelve los detalles de un carrito específico según el ID del usuario.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Carrito>> obtenerCarrito(@PathVariable Long usuarioId) {
    Carrito carrito = carritoService.obtenerOCrearCarrito(usuarioId);
    EntityModel<Carrito> model = EntityModel.of(carrito,
            linkTo(methodOn(CarritoController.class).obtenerCarrito(usuarioId)).withSelfRel(),
            linkTo(methodOn(CarritoController.class).vaciarCarrito(usuarioId)).withRel("vaciar"));
    return ResponseEntity.ok(model);
  }

  @PostMapping("/usuario/{usuarioId}/agregar")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
  @Operation(summary = "Agregar un nuevo producto al carrito", description = "Agrega un nuevo producto al carrito del usuario y devuelve los detalles del carrito actualizado.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Producto agregado al carrito exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Carrito>> agregarProducto(
      @PathVariable Long usuarioId,
      @RequestParam Long productoId,
      @RequestParam Integer cantidad) {
    Carrito carrito = carritoService.agregarProductoAlCarrito(usuarioId, productoId, cantidad);
    EntityModel<Carrito> model = EntityModel.of(carrito,
            linkTo(methodOn(CarritoController.class).obtenerCarrito(usuarioId)).withRel("carrito"),
            linkTo(methodOn(CarritoController.class).vaciarCarrito(usuarioId)).withRel("vaciar"));
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/usuario/{usuarioId}/item/{detalleId}")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
  @Operation(summary = "Eliminar un producto del carrito", description = "Elimina un producto del carrito del usuario según su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Carrito>> eliminarItem(
      @PathVariable Long usuarioId,
      @PathVariable Long detalleId) {
    Carrito carrito = carritoService.eliminarItemDelCarrito(usuarioId, detalleId);
    EntityModel<Carrito> model = EntityModel.of(carrito,
            linkTo(methodOn(CarritoController.class).obtenerCarrito(usuarioId)).withRel("carrito"),
            linkTo(methodOn(CarritoController.class).vaciarCarrito(usuarioId)).withRel("vaciar"));
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/usuario/{usuarioId}/vaciar")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
  @Operation(summary = "Vaciar el carrito", description = "Elimina todos los productos del carrito del usuario.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Carrito vaciado exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Map<String, String>> vaciarCarrito(@PathVariable Long usuarioId) {
    carritoService.vaciarCarrito(usuarioId);
    return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado correctamente."));
  }
}