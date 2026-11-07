package api_app_mapas.perfil.controller;

import api_app_mapas.perfil.dto.ActualizarPerfilRequest;
import api_app_mapas.perfil.dto.ImagenPerfilResponse;
import api_app_mapas.perfil.dto.PerfilResponse;
import api_app_mapas.perfil.dto.ActividadResponse;
import api_app_mapas.perfil.dto.CambiarPasswordRequest;
import api_app_mapas.perfil.dto.CambiarPasswordResponse;
import api_app_mapas.perfil.service.PerfilService;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.lugares.repository.LugarRepository;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.reportes.repository.ReporteSeguridadRepository;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final SesionService sesionService;
    private final PerfilService perfilService;
    private final LugarRepository lugares;
    private final ResenaLugarRepository resenas;
    private final ReporteSeguridadRepository reportes;

    public PerfilController(SesionService sesionService, PerfilService perfilService,
                            LugarRepository lugares, ResenaLugarRepository resenas,
                            ReporteSeguridadRepository reportes) {
        this.sesionService = sesionService;
        this.perfilService = perfilService;
        this.lugares = lugares;
        this.resenas = resenas;
        this.reportes = reportes;
    }

    @GetMapping
    public PerfilResponse obtener(@RequestHeader("Authorization") String authorization) {
        return perfilService.obtener(sesionService.obtenerUsuario(authorization));
    }

    @GetMapping("/actividad")
    public List<ActividadResponse> actividad(@RequestHeader("Authorization") String authorization) {
        Integer userId=sesionService.obtenerUsuario(authorization).getIdUsuario();
        List<ActividadResponse> result=new ArrayList<>();
        lugares.findByCreador_IdUsuarioOrderByFechaCreacionDesc(userId).forEach(l ->
                result.add(new ActividadResponse("LUGAR",l.getIdLugar(),l.getNombre(),
                        "Lugar registrado · "+l.getTipo(),l.getFechaCreacion(),l.getTipo(),"",0,l.getIdLugar(),null)));
        resenas.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(userId).forEach(r ->
                result.add(new ActividadResponse("RESENA",r.getId(),r.getLugar().getNombre(),
                        "Reseña de "+r.getEstrellas()+" estrellas",r.getFechaCreacion(),
                        r.getLugar().getTipo(),"",r.getEstrellas(),r.getLugar().getIdLugar(),null)));
        reportes.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(userId).forEach(r ->
                result.add(new ActividadResponse("REPORTE",r.getIdReporte(),r.getTipoEvento(),
                        "Reporte "+r.getEstadoValidacion().toLowerCase().replace('_',' '),r.getFechaCreacion(),
                        r.getTipoEvento(),r.getEstadoValidacion(),0,r.getIdReporte(),r.getFechaExpiracion())));
        result.sort(Comparator.comparing(ActividadResponse::fecha,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return result;
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

    @PutMapping("/contrasena")
    public CambiarPasswordResponse cambiarPassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CambiarPasswordRequest request
    ) {
        perfilService.cambiarPassword(
                sesionService.obtenerUsuario(authorization), request);
        return new CambiarPasswordResponse(true,
                "Contraseña actualizada correctamente");
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
