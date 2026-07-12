package com.minimarket.controller;

import com.minimarket.dto.UsuarioResponseDTO;
import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import com.minimarket.dto.ErrorResponseDTO;
import com.minimarket.dto.UsuarioRequestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuario", description = "API para gestionar usuarios del sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(
            summary = "Obtener lista de usuarios",
            description = "Devuelve una lista paginada de todos los usuarios"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<PagedModel<EntityModel<UsuarioResponseDTO>>> listarUsuarios(
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (asc o desc)") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Usuario> usuariosPage = usuarioService.findAll(pageable);

        List<EntityModel<UsuarioResponseDTO>> usuariosModel =
                usuariosPage.getContent()
                        .stream()
                        .map(usuario -> EntityModel.of(
                                convertirADTO(usuario),

                                linkTo(methodOn(UsuarioController.class)
                                        .obtenerUsuarioPorId(usuario.getId()))
                                        .withSelfRel(),

                                linkTo(methodOn(UsuarioController.class)
                                        .actualizarUsuario(usuario.getId(), null))
                                        .withRel("update"),

                                linkTo(methodOn(UsuarioController.class)
                                        .eliminarUsuario(usuario.getId()))
                                        .withRel("delete")
                        ))
                        .collect(Collectors.toList());

        PageMetadata metadata = new PageMetadata(
                usuariosPage.getSize(),
                usuariosPage.getNumber(),
                usuariosPage.getTotalElements(),
                usuariosPage.getTotalPages()
        );

        PagedModel<EntityModel<UsuarioResponseDTO>> pagedModel =
                PagedModel.of(usuariosModel, metadata);

        pagedModel.add(
                linkTo(methodOn(UsuarioController.class)
                        .listarUsuarios(page, size, sortBy, sortDir))
                        .withSelfRel()
        );

        if (usuariosPage.hasPrevious()) {
            pagedModel.add(
                    linkTo(methodOn(UsuarioController.class)
                            .listarUsuarios(
                                    usuariosPage.getNumber() - 1,
                                    size,
                                    sortBy,
                                    sortDir))
                            .withRel("prev")
            );
        }

        if (usuariosPage.hasNext()) {
            pagedModel.add(
                    linkTo(methodOn(UsuarioController.class)
                            .listarUsuarios(
                                    usuariosPage.getNumber() + 1,
                                    size,
                                    sortBy,
                                    sortDir))
                            .withRel("next")
            );
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Devuelve los detalles de un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario con el ID especificado no fue encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> obtenerUsuarioPorId(
            @Parameter(description = "ID único del usuario") @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.findById(id);

        return usuario.map(u -> {
            EntityModel<UsuarioResponseDTO> model = EntityModel.of(
                    convertirADTO(u),

                    linkTo(methodOn(UsuarioController.class)
                            .obtenerUsuarioPorId(id))
                            .withSelfRel(),

                    linkTo(methodOn(UsuarioController.class)
                            .listarUsuarios(0, 10, "id", "asc"))
                            .withRel("allUsuarios"),

                    linkTo(methodOn(UsuarioController.class)
                            .actualizarUsuario(id, null))
                            .withRel("update"),

                    linkTo(methodOn(UsuarioController.class)
                            .eliminarUsuario(id))
                            .withRel("delete")
            );

            return ResponseEntity.ok(model);

        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "Guardar un nuevo usuario",
            description = "Crea un nuevo usuario en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))
            )
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> guardarUsuario(
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {

        Usuario nuevoUsuario = usuarioService.save(usuarioDTO.toEntity());

        EntityModel<UsuarioResponseDTO> model = EntityModel.of(
                convertirADTO(nuevoUsuario),

                linkTo(methodOn(UsuarioController.class)
                        .obtenerUsuarioPorId(nuevoUsuario.getId()))
                        .withSelfRel(),

                linkTo(methodOn(UsuarioController.class)
                        .listarUsuarios(0, 10, "id", "asc"))
                        .withRel("allUsuarios"),

                linkTo(methodOn(UsuarioController.class)
                        .actualizarUsuario(nuevoUsuario.getId(), null))
                        .withRel("update"),

                linkTo(methodOn(UsuarioController.class)
                        .eliminarUsuario(nuevoUsuario.getId()))
                        .withRel("delete")
        );

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos de un usuario existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario con el ID especificado no fue encontrado para actualizar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> actualizarUsuario(
            @Parameter(description = "ID único del usuario a actualizar") @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {

        Optional<Usuario> usuarioExistente = usuarioService.findById(id);

        if (usuarioExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioDTO.toEntity();
        usuario.setId(id);
        Usuario actualizado = usuarioService.save(usuario);

        EntityModel<UsuarioResponseDTO> model = EntityModel.of(
                convertirADTO(actualizado),

                linkTo(methodOn(UsuarioController.class)
                        .obtenerUsuarioPorId(id))
                        .withSelfRel(),

                linkTo(methodOn(UsuarioController.class)
                        .listarUsuarios(0, 10, "id", "asc"))
                        .withRel("allUsuarios"),

                linkTo(methodOn(UsuarioController.class)
                        .eliminarUsuario(id))
                        .withRel("delete")
        );

        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario por su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Usuario con el ID especificado no fue encontrado para eliminar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarUsuario(
            @Parameter(description = "ID único del usuario a eliminar") @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.findById(id);

        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        usuarioService.deleteById(id);

        EntityModel<Map<String, String>> responseModel = EntityModel.of(
                Map.of("message", "Usuario eliminado exitosamente"),

                linkTo(methodOn(UsuarioController.class)
                        .listarUsuarios(0, 10, "id", "asc"))
                        .withRel("allUsuarios"),

                linkTo(methodOn(UsuarioController.class)
                        .guardarUsuario(null))
                        .withRel("addUsuario")
        );

        return ResponseEntity.ok(responseModel);
    }

    private UsuarioResponseDTO convertirADTO(Usuario usuario) {
        Set<String> roles = usuario.getRoles() == null
                ? Set.of()
                : usuario.getRoles()
                        .stream()
                        .map(rol -> rol.getNombre())
                        .collect(Collectors.toSet());

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                roles
        );
    }
}