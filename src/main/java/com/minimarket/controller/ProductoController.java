package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Producto", description = "API para gestionar productos, incluyendo operaciones CRUD y consultas.")
public class ProductoController {

  @Autowired
  private ProductoService productoService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
  @Operation(summary = "Obtener lista de productos", description = "Devuelve una lista de todos los productos disponibles en el sistema. Requiere autenticación.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public CollectionModel<EntityModel<Producto>> listarTodos() {
    List<EntityModel<Producto>> productos = productoService.findAll().stream()
            .map(producto -> EntityModel.of(producto,
                    linkTo(methodOn(ProductoController.class).listarTodos()).withRel("allProductos"),
                    linkTo(methodOn(ProductoController.class).actualizarProducto(producto.getId(), producto)).withRel("update"),
                    linkTo(methodOn(ProductoController.class).eliminarProducto(producto.getId())).withRel("delete")))
            .collect(Collectors.toList());
    return CollectionModel.of(productos, linkTo(methodOn(ProductoController.class).listarTodos()).withSelfRel());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  @Operation(summary = "Agregar un nuevo producto", description = "Crea un nuevo producto en el sistema y devuelve los detalles del producto creado.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Producto>> crearProducto(@RequestBody Producto producto) {
    Producto guardado = productoService.save(producto);
    EntityModel<Producto> model = EntityModel.of(guardado,
            linkTo(methodOn(ProductoController.class).listarTodos()).withRel("allProductos"),
            linkTo(methodOn(ProductoController.class).actualizarProducto(guardado.getId(), guardado)).withRel("update"),
            linkTo(methodOn(ProductoController.class).eliminarProducto(guardado.getId())).withRel("delete"));
    return ResponseEntity.ok(model);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  @Operation(summary = "Actualizar un producto existente", description = "Actualiza los detalles de un producto existente según su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
      @ApiResponse(responseCode = "404", description = "Producto no encontrado")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
    producto.setId(id);
    Producto actualizado = productoService.save(producto);
    EntityModel<Producto> model = EntityModel.of(actualizado,
            linkTo(methodOn(ProductoController.class).actualizarProducto(id, actualizado)).withSelfRel(),
            linkTo(methodOn(ProductoController.class).listarTodos()).withRel("allProductos"),
            linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("delete"));
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  @Operation(summary = "Eliminar un producto", description = "Elimina un producto del sistema según su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(@PathVariable Long id) {
    productoService.deleteById(id);
    EntityModel<Map<String, String>> responseModel = EntityModel.of(Map.of("message", "Producto eliminado exitosamente"),
        linkTo(methodOn(ProductoController.class).listarTodos()).withRel("allProductos"),
        linkTo(methodOn(ProductoController.class).crearProducto(new Producto())).withRel("addProducto")
    );
    return ResponseEntity.ok(responseModel);
  }
}