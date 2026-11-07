package api_app_mapas.reportes.controller;

import api_app_mapas.reportes.dto.*;
import api_app_mapas.reportes.repository.ReporteSeguridadRepository;
import api_app_mapas.reportes.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController @RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteSeguridadRepository repository; private final ReporteService service; private final JdbcTemplate jdbc;
    public ReporteController(ReporteSeguridadRepository repository, ReporteService service,JdbcTemplate jdbc) { this.repository=repository; this.service=service;this.jdbc=jdbc; }
    @GetMapping public List<ReporteResponse> listar(@RequestHeader(value="Authorization",required=false) String auth,@RequestParam BigDecimal sur,@RequestParam BigDecimal norte,@RequestParam BigDecimal oeste,@RequestParam BigDecimal este) {
        if(sur.compareTo(norte)>0||oeste.compareTo(este)>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Coordenadas no válidas");
        return repository.buscarEnVista(sur,norte,oeste,este, PageRequest.of(0,300)).stream().map(r -> ReporteResponse.desde(r,service.esPropio(auth,r))).toList();
    }
    @GetMapping("/{id}") public ReporteResponse detalle(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable Long id){var r=service.obtener(id);return ReporteResponse.desde(r,service.esPropio(auth,r));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ReporteResponse crear(@RequestHeader("Authorization") String auth,@Valid @RequestBody CrearReporteRequest request){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La imagen del reporte es obligatoria");
    }
    @PostMapping(value="/con-imagen",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse crearConImagen(@RequestHeader("Authorization") String auth,
            @RequestParam String tipoEvento,@RequestParam String nivelPeligro,@RequestParam String descripcion,
            @RequestParam BigDecimal latitud,@RequestParam BigDecimal longitud,@RequestParam Integer radioMetros,
            @RequestParam Integer duracionHoras,@RequestParam(defaultValue="") String distrito,
            @RequestParam(defaultValue="false") boolean anonimo,@RequestPart("imagen") MultipartFile imagen) throws IOException {
        CrearReporteRequest request=new CrearReporteRequest(tipoEvento,nivelPeligro,descripcion,latitud,longitud,
                radioMetros,duracionHoras,distrito,anonimo);
        return service.crearConImagen(auth,request,imagen);
    }
    @GetMapping("/mios") public List<ReporteResponse> mios(@RequestHeader("Authorization") String auth){
        var usuario=service.usuario(auth);return repository.listarActivosDelUsuario(usuario.getIdUsuario()).stream()
                .map(r->ReporteResponse.desde(r,true)).toList();
    }
    @GetMapping("/{id}/comentarios") public List<ComentarioReporteResponse> comentarios(@PathVariable Long id){return service.comentarios(id);}
    @GetMapping("/{id}/comentarios/pagina") public ComentariosPageResponse comentariosPagina(@PathVariable Long id,
            @RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="5") int limite){return service.comentariosPagina(id,pagina,limite);}
    @PostMapping("/{id}/comentarios") @ResponseStatus(HttpStatus.CREATED) public ComentarioReporteResponse comentar(@RequestHeader("Authorization") String auth,@PathVariable Long id,@Valid @RequestBody TextoRequest request){return service.comentar(auth,id,request);}
    @PostMapping(value="/{id}/comentarios/con-imagen",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public ComentarioReporteResponse comentarConImagen(@RequestHeader("Authorization") String auth,@PathVariable Long id,
            @RequestParam String texto,@RequestPart(value="imagen",required=false) MultipartFile imagen) throws IOException {
        return service.comentarConImagen(auth,id,texto,imagen);
    }
    @PutMapping("/{id}/voto") public ReporteResponse votar(@RequestHeader("Authorization") String auth,@PathVariable Long id,@Valid @RequestBody VotoRequest request){return service.votar(auth,id,request);}
    public record AlertaRequest(String nivel) {}
    @PostMapping("/{id}/alerta") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrarAlerta(@RequestHeader("Authorization") String auth,@PathVariable Long id,@RequestBody AlertaRequest request){
        service.obtener(id);String nivel=request.nivel();if(!java.util.Set.of("500","50","inside").contains(nivel))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Nivel de alerta inválido");
        jdbc.update("INSERT IGNORE INTO eventos_alerta_usuario (id_usuario,id_reporte,nivel) VALUES (?,?,?)",service.usuario(auth).getIdUsuario(),id,nivel);
    }
    @PostMapping("/{id}/reportar") @ResponseStatus(HttpStatus.NO_CONTENT) public void reportar(@RequestHeader("Authorization") String auth,@PathVariable Long id,@Valid @RequestBody TextoRequest request){service.reportar(auth,id,request);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@RequestHeader("Authorization") String auth,@PathVariable Long id){service.eliminarPropio(auth,id);}
    @PostMapping("/contenido") @ResponseStatus(HttpStatus.CREATED)
    public ReporteContenidoResponse reportarContenido(@RequestHeader("Authorization") String auth,
            @Valid @RequestBody CrearReporteContenidoRequest request) {
        return service.reportarContenido(auth, request);
    }
}
