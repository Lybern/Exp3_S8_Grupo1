package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
import com.minimarket.dto.CategoriaRequestDTO;
import com.minimarket.dto.CategoriaResponseDTO;
import com.minimarket.dto.ErrorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categoria", description = "API para gestionar categorías")
public class CategoriaController {
    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar todas las categorías")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(mediaType = "application/json"))
    })
    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaService.findAll().stream().map(CategoriaResponseDTO::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> obtenerCategoriaPorId(@Parameter(description = "ID de la categoría") @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(CategoriaResponseDTO.from(categoria)) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear categoría")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> guardarCategoria(@Valid @RequestBody CategoriaRequestDTO dto) {
        Categoria guardada = categoriaService.save(dto.toEntity());
        return ResponseEntity.status(201).body(CategoriaResponseDTO.from(guardada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(@Parameter(description = "ID de la categoría") @PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO dto) {
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            Categoria c = dto.toEntity();
            c.setId(id);
            return ResponseEntity.ok(CategoriaResponseDTO.from(categoriaService.save(c)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría eliminada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminarCategoria(@Parameter(description = "ID de la categoría") @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
