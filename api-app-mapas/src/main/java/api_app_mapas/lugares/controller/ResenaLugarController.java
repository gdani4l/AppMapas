package api_app_mapas.lugares.controller;

import api_app_mapas.lugares.dto.*;
import api_app_mapas.lugares.service.ResenaLugarService;
import api_app_mapas.sesion.service.SesionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/lugares/{lugarId}/resenas")
public class ResenaLugarController {
    private final ResenaLugarService service;
    private final SesionService sesiones;

    public ResenaLugarController(ResenaLugarService service, SesionService sesiones) {
        this.service = service;
        this.sesiones = sesiones;
    }

    @GetMapping
    public ResenasPageResponse listar(@PathVariable Long lugarId,
                                      @RequestParam(defaultValue = "0") int pagina,
                                      @RequestParam(defaultValue = "3") int limite) {
        return service.listar(lugarId, pagina, limite);
    }

    @GetMapping("/imagenes")
    public List<String> imagenes(@PathVariable Long lugarId) {
        return service.imagenes(lugarId);
    }

    @GetMapping("/fotos")
    public List<FotoLugarResponse> fotos(@PathVariable Long lugarId) {
        return service.fotos(lugarId);
    }

    @GetMapping("/mia")
    public ResponseEntity<ResenaResponse> mia(@PathVariable Long lugarId,
                              @RequestHeader("Authorization") String authorization) {
        ResenaResponse mia = service.mia(lugarId, sesiones.obtenerUsuario(authorization));
        return mia == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(mia);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaResponse crear(@PathVariable Long lugarId,
            @RequestHeader("Authorization") String authorization,
            @RequestParam Integer estrellas, @RequestParam(required = false) String comentario,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        return service.guardar(lugarId, sesiones.obtenerUsuario(authorization),
                estrellas, comentario, imagen, false, false);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResenaResponse editar(@PathVariable Long lugarId,
            @RequestHeader("Authorization") String authorization,
            @RequestParam Integer estrellas, @RequestParam(required = false) String comentario,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(defaultValue = "false") boolean quitarImagen) throws IOException {
        return service.guardar(lugarId, sesiones.obtenerUsuario(authorization),
                estrellas, comentario, imagen, true, quitarImagen);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long lugarId,
                         @RequestHeader("Authorization") String authorization) {
        service.eliminar(lugarId, sesiones.obtenerUsuario(authorization));
    }
}
