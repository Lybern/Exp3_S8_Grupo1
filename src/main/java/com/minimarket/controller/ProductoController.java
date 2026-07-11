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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;

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
  @Operation(summary = "Obtener lista de productos", description = "Devuelve una lista paginada de todos los productos disponibles en el sistema. Requiere autenticación.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.minimarket.dto.ProductoPageResponseDTO.class)))
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<PagedModel<EntityModel<com.minimarket.dto.ProductoResponseDTO>>> listarTodos(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "nombre") String sortBy,
    @RequestParam(defaultValue = "asc") String sortDir
  ) {
    Sort sort = sortDir.equalsIgnoreCase("asc") 
    ? Sort.by(sortBy).ascending() 
    : Sort.by(sortBy).descending();
    
    Pageable pageable = PageRequest.of(page, size, sort);
    
    Page<Producto> productosPage = productoService.findAll(pageable);
    List<EntityModel<com.minimarket.dto.ProductoResponseDTO>> productosModel = productosPage.getContent().stream()
            .map(producto -> EntityModel.of(com.minimarket.dto.ProductoResponseDTO.from(producto),
                    linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                    linkTo(methodOn(ProductoController.class).listarTodos(page, size, sortBy, sortDir)).withRel("allProductos"),
                    linkTo(methodOn(ProductoController.class).actualizarProducto(producto.getId(), producto)).withRel("update"),
                    linkTo(methodOn(ProductoController.class).eliminarProducto(producto.getId())).withRel("delete")))
            .collect(Collectors.toList());
            
    PageMetadata metadata = new PageMetadata(productosPage.getSize(), productosPage.getNumber(), productosPage.getTotalElements(), productosPage.getTotalPages());
    PagedModel<EntityModel<com.minimarket.dto.ProductoResponseDTO>> pagedModel = PagedModel.of(productosModel, metadata);
    
    pagedModel.add(
        linkTo(methodOn(ProductoController.class).listarTodos(page, size, sortBy, sortDir)).withSelfRel(),
        linkTo(methodOn(ProductoController.class).listarTodos(0, size, sortBy, sortDir)).withRel("first"),
        linkTo(methodOn(ProductoController.class).listarTodos(productosPage.getTotalPages() == 0 ? 0 : productosPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last")
    );
    
    if (productosPage.hasPrevious()) {
        pagedModel.add(linkTo(methodOn(ProductoController.class).listarTodos(productosPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
    }
    if (productosPage.hasNext()) {
        pagedModel.add(linkTo(methodOn(ProductoController.class).listarTodos(productosPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
    }
    
    return ResponseEntity.ok(pagedModel);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'CLIENTE')")
  @Operation(summary = "Obtener producto por ID", description = "Devuelve los detalles de un producto específico.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto encontrado"),
      @ApiResponse(responseCode = "404", description = "Producto no encontrado")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<com.minimarket.dto.ProductoResponseDTO>> obtenerProductoPorId(@PathVariable Long id) {
      Producto p = productoService.findById(id);
      if (p != null) {
          EntityModel<com.minimarket.dto.ProductoResponseDTO> model = EntityModel.of(com.minimarket.dto.ProductoResponseDTO.from(p),
                  linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withSelfRel(),
                  linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
                  linkTo(methodOn(ProductoController.class).actualizarProducto(id, p)).withRel("update"),
                  linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("delete"));
          return ResponseEntity.ok(model);
      }
      return ResponseEntity.notFound().build();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  @Operation(summary = "Agregar un nuevo producto", description = "Crea un nuevo producto en el sistema y devuelve los detalles del producto creado.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<com.minimarket.dto.ProductoResponseDTO>> crearProducto(@RequestBody Producto producto) {
    Producto guardado = productoService.save(producto);
    
    // Se agregan enlaces HATEOAS para guiar al usuario a las siguientes acciones posibles
    EntityModel<com.minimarket.dto.ProductoResponseDTO> model = EntityModel.of(com.minimarket.dto.ProductoResponseDTO.from(guardado),
            linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
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
  public ResponseEntity<EntityModel<com.minimarket.dto.ProductoResponseDTO>> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
    producto.setId(id);
    Producto actualizado = productoService.save(producto);
    
    // HATEOAS: Se incluye enlace a sí mismo (update), a la lista total y a la opción de eliminar
    EntityModel<com.minimarket.dto.ProductoResponseDTO> model = EntityModel.of(com.minimarket.dto.ProductoResponseDTO.from(actualizado),
            linkTo(methodOn(ProductoController.class).actualizarProducto(id, actualizado)).withSelfRel(),
            linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
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
    
    // HATEOAS: Como el producto ya no existe, sugerimos volver a la lista o crear uno nuevo
    EntityModel<Map<String, String>> responseModel = EntityModel.of(Map.of("message", "Producto eliminado exitosamente"),
        linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
        linkTo(methodOn(ProductoController.class).crearProducto(new Producto())).withRel("addProducto")
    );
    
    return ResponseEntity.ok(responseModel);
  }
}