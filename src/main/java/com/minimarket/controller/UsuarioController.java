package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuario", description = "API para gestionar usuarios del sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Obtener lista de usuarios", description = "Devuelve una lista paginada de todos los usuarios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    })
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarUsuarios(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Usuario> usuariosPage = usuarioService.findAll(pageable);
        
        List<EntityModel<Usuario>> usuariosModel = usuariosPage.getContent().stream()
                .map(usuario -> EntityModel.of(usuario,
                        linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getId())).withSelfRel(),
                        linkTo(methodOn(UsuarioController.class).actualizarUsuario(usuario.getId(), usuario)).withRel("update"),
                        linkTo(methodOn(UsuarioController.class).eliminarUsuario(usuario.getId())).withRel("delete")))
                .collect(Collectors.toList());

        PageMetadata metadata = new PageMetadata(usuariosPage.getSize(), usuariosPage.getNumber(), usuariosPage.getTotalElements(), usuariosPage.getTotalPages());
        PagedModel<EntityModel<Usuario>> pagedModel = PagedModel.of(usuariosModel, metadata);
        
        pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(page, size, sortBy, sortDir)).withSelfRel());
        
        if (usuariosPage.hasPrevious()) {
            pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(usuariosPage.getNumber() - 1, size, sortBy, sortDir)).withRel("prev"));
        }
        if (usuariosPage.hasNext()) {
            pagedModel.add(linkTo(methodOn(UsuarioController.class).listarUsuarios(usuariosPage.getNumber() + 1, size, sortBy, sortDir)).withRel("next"));
        }

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve los detalles de un usuario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<Usuario>> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(u -> {
            EntityModel<Usuario> model = EntityModel.of(u,
                    linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id)).withSelfRel(),
                    linkTo(methodOn(UsuarioController.class).listarUsuarios(0, 10, "id", "asc")).withRel("allUsuarios"),
                    linkTo(methodOn(UsuarioController.class).actualizarUsuario(id, u)).withRel("update"),
                    linkTo(methodOn(UsuarioController.class).eliminarUsuario(id)).withRel("delete"));
            return ResponseEntity.ok(model);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Guardar un nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente")
    })
    public ResponseEntity<EntityModel<Usuario>> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.save(usuario);
        EntityModel<Usuario> model = EntityModel.of(nuevoUsuario,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(nuevoUsuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios(0, 10, "id", "asc")).withRel("allUsuarios"),
                linkTo(methodOn(UsuarioController.class).actualizarUsuario(nuevoUsuario.getId(), nuevoUsuario)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).eliminarUsuario(nuevoUsuario.getId())).withRel("delete"));
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<Usuario>> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuario.setId(id);
            Usuario actualizado = usuarioService.save(usuario);
            EntityModel<Usuario> model = EntityModel.of(actualizado,
                    linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id)).withSelfRel(),
                    linkTo(methodOn(UsuarioController.class).listarUsuarios(0, 10, "id", "asc")).withRel("allUsuarios"),
                    linkTo(methodOn(UsuarioController.class).eliminarUsuario(id)).withRel("delete"));
            return ResponseEntity.ok(model);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<Map<String, String>>> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            EntityModel<Map<String, String>> responseModel = EntityModel.of(Map.of("message", "Usuario eliminado exitosamente"),
                linkTo(methodOn(UsuarioController.class).listarUsuarios(0, 10, "id", "asc")).withRel("allUsuarios"),
                linkTo(methodOn(UsuarioController.class).guardarUsuario(new Usuario())).withRel("addUsuario")
            );
            return ResponseEntity.ok(responseModel);
        }
        return ResponseEntity.notFound().build();
    }
}
