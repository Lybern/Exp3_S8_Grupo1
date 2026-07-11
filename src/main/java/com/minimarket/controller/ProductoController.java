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
    
    // 1. Obtener la página cruda desde la base de datos
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Producto> productosPage = productoService.findAll(pageable);
    
    // 2. Transformar la lista de entidades a una lista de DTOs envueltos en EntityModel (HATEOAS)
    List<EntityModel<com.minimarket.dto.ProductoResponseDTO>> productosModel = productosPage.getContent().stream()
            .map(producto -> EntityModel.of(com.minimarket.dto.ProductoResponseDTO.from(producto),
                    linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                    linkTo(methodOn(ProductoController.class).listarTodos(page, size, sortBy, sortDir)).withRel("allProductos"),
                    linkTo(methodOn(ProductoController.class).actualizarProducto(producto.getId(), producto)).withRel("update"),
                    linkTo(methodOn(ProductoController.class).eliminarProducto(producto.getId())).withRel("delete")))
            .collect(Collectors.toList());
            
    // 3. Crear los metadatos de paginación para el PagedModel
    PageMetadata metadata = new PageMetadata(productosPage.getSize(), productosPage.getNumber(), productosPage.getTotalElements(), productosPage.getTotalPages());
    PagedModel<EntityModel<com.minimarket.dto.ProductoResponseDTO>> pagedModel = PagedModel.of(productosModel, metadata);
    
    // 4. Agregar los enlaces globales de navegación de la página (self, first, last)
    pagedModel.add(
        linkTo(methodOn(ProductoController.class).listarTodos(page, size, sortBy, sortDir)).withSelfRel(),
        linkTo(methodOn(ProductoController.class).listarTodos(0, size, sortBy, sortDir)).withRel("first"),
        linkTo(methodOn(ProductoController.class).listarTodos(productosPage.getTotalPages() == 0 ? 0 : productosPage.getTotalPages() - 1, size, sortBy, sortDir)).withRel("last")
    );
    
    // 5. Agregar dinámicamente prev/next si existen páginas adyacentes
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
      
      // Si el producto existe, lo transformamos a DTO y le inyectamos sus enlaces dinámicos
      if (p != null) {
          EntityModel<com.minimarket.dto.ProductoResponseDTO> model = EntityModel.of(com.minimarket.dto.ProductoResponseDTO.from(p),
                  linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withSelfRel(),
                  linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
                  linkTo(methodOn(ProductoController.class).actualizarProducto(id, p)).withRel("update"),
                  linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("delete"));
          return ResponseEntity.ok(model);
      }
      
      // Si no existe, devolvemos un 404 estandarizado
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
    
    // =========================================================================================
    // HATEOAS A NIVEL DE CREACIÓN (POST):
    // Una vez que el producto se crea exitosamente en la Base de Datos, no solo devolvemos los datos,
    // sino que construimos un EntityModel para inyectar enlaces de Navegación (Hipermedia).
    // 
    // Funciones clave de Spring HATEOAS:
    // - linkTo(): Inspecciona la clase del Controlador para saber cuál es la ruta base (ej. /api/productos)
    // - methodOn(): Simula una llamada al método destino para extraer sus parámetros y construir la URL final dinámicamente.
    // - withRel("nombre"): Asigna la etiqueta semántica ("rel") que el cliente leerá en el JSON.
    // =========================================================================================
    EntityModel<com.minimarket.dto.ProductoResponseDTO> model = EntityModel.of(
            com.minimarket.dto.ProductoResponseDTO.from(guardado), // 1. Protegemos la entidad inyectando el DTO
            
            // 2. Enlace hacia la lista completa de productos.
            linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
            
            // 3. Enlace con la operación HTTP PUT que permitiría actualizar este recurso recién creado.
            linkTo(methodOn(ProductoController.class).actualizarProducto(guardado.getId(), guardado)).withRel("update"),
            
            // 4. Enlace con la operación HTTP DELETE que permitiría destruir este recurso.
            linkTo(methodOn(ProductoController.class).eliminarProducto(guardado.getId())).withRel("delete")
    );
            
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
    
    // =========================================================================================
    // HATEOAS A NIVEL DE ACTUALIZACIÓN (PUT):
    // Se implementa el concepto fundamental de "self". 
    // Cuando un cliente actualiza un recurso, el enlace principal que recibe de vuelta es 
    // la propia URL que acaba de golpear.
    // =========================================================================================
    EntityModel<com.minimarket.dto.ProductoResponseDTO> model = EntityModel.of(
            com.minimarket.dto.ProductoResponseDTO.from(actualizado),
            
            // withSelfRel(): Es un estándar REST. Etiqueta automáticamente el enlace como "self", indicando
            // que esta es la URL de autoridad para el estado actual de este recurso.
            linkTo(methodOn(ProductoController.class).actualizarProducto(id, actualizado)).withSelfRel(),
            
            // Navegación hacia recursos hermanos (volver a la lista completa).
            linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
            
            // Permite al cliente descubrir dinámicamente cómo borrar lo que acaba de actualizar.
            linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("delete")
    );
            
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  @Operation(summary = "Eliminar un producto", description = "Elimina un producto del sistema según su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
      @ApiResponse(responseCode = "404", description = "Producto no encontrado")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<EntityModel<Map<String, String>>> eliminarProducto(@PathVariable Long id) {
    Producto existente = productoService.findById(id);
    if (existente == null) {
        return ResponseEntity.notFound().build();
    }
    
    productoService.deleteById(id);
    
    // =========================================================================================
    // HATEOAS A NIVEL DE ELIMINACIÓN (DELETE):
    // Una vez que el recurso es destruido (DELETE), no tiene sentido devolver un "self" a un 
    // producto que ya no existe (daría 404). 
    // 
    // Por ende, se utiliza HATEOAS para guiar al usuario hacia el siguiente flujo lógico:
    // 1. "allProductos": El enlace para volver a ver el inventario restante.
    // 2. "addProducto": El enlace por si desea revertir la acción creando un producto nuevo.
    // =========================================================================================
    EntityModel<Map<String, String>> responseModel = EntityModel.of(
        Map.of("message", "Producto eliminado exitosamente"), // Payload de respuesta genérico
        
        // Guía al cliente de vuelta al listado
        linkTo(methodOn(ProductoController.class).listarTodos(0, 10, "nombre", "asc")).withRel("allProductos"),
        
        // Le enseña al cliente cuál es el endpoint (POST) para crear registros
        linkTo(methodOn(ProductoController.class).crearProducto(new Producto())).withRel("addProducto")
    );
    
    return ResponseEntity.ok(responseModel);
  }
}