package api_app_mapas.perfil.controller;

import api_app_mapas.perfil.dto.ActualizarPerfilRequest;
import api_app_mapas.perfil.dto.ImagenPerfilResponse;
import api_app_mapas.perfil.dto.PerfilResponse;
import api_app_mapas.perfil.service.PerfilService;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final SesionService sesionService;
    private final PerfilService perfilService;

    public PerfilController(SesionService sesionService, PerfilService perfilService) {
        this.sesionService = sesionService;
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilResponse obtener(@RequestHeader("Authorization") String authorization) {
        return perfilService.obtener(sesionService.obtenerUsuario(authorization));
    }

    @PutMapping
    public PerfilResponse actualizar(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ActualizarPerfilRequest request
    ) {
        return perfilService.actualizar(
                sesionService.obtenerUsuario(authorization),
                request
        );
    }

    @PostMapping(value = "/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenPerfilResponse> actualizarImagen(
            @RequestHeader("Authorization") String authorization,
            @RequestPart("imagen") MultipartFile imagen
    ) throws Exception {
        if (imagen.isEmpty() || imagen.getContentType() == null
                || !imagen.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(BAD_REQUEST, "Selecciona una imagen válida");
        }

        Usuario usuario = sesionService.obtenerUsuario(authorization);
        String url = perfilService.actualizarImagen(usuario, imagen.getBytes());
        return ResponseEntity.ok(new ImagenPerfilResponse(
                true,
                "Imagen de perfil actualizada",
                url
        ));
    }
}
