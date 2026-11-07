package api_app_mapas.administracion.controller;

import api_app_mapas.lugares.repository.LugarRepository;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.reportes.dto.ReporteContenidoResponse;
import api_app_mapas.reportes.dto.ReporteResponse;
import api_app_mapas.lugares.dto.LugarResponse;
import api_app_mapas.reportes.repository.ReporteContenidoRepository;
import api_app_mapas.reportes.repository.ReporteSeguridadRepository;
import api_app_mapas.reportes.model.ReporteContenido;
import api_app_mapas.sesion.service.SesionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/admin")
public class AdministracionController {
    private final SesionService sesiones;
    private final ReporteContenidoRepository denuncias;
    private final ReporteSeguridadRepository reportes;
    private final LugarRepository lugares;
    private final ResenaLugarRepository resenas;
    public AdministracionController(SesionService sesiones, ReporteContenidoRepository denuncias,
            ReporteSeguridadRepository reportes, LugarRepository lugares, ResenaLugarRepository resenas) {
        this.sesiones=sesiones; this.denuncias=denuncias; this.reportes=reportes;
        this.lugares=lugares; this.resenas=resenas;
    }
    @GetMapping("/denuncias")
    public List<ReporteContenidoResponse> denuncias(@RequestHeader("Authorization") String auth) {
        requireAdmin(auth);
        return denuncias.findByEstadoOrderByFechaCreacionAsc("PENDIENTE").stream()
                .map(this::contenidoDenunciado).toList();
    }
    @DeleteMapping("/denuncias/{id}/contenido")
    public void eliminarContenido(@RequestHeader("Authorization") String auth,@PathVariable Long id) {
        requireAdmin(auth);
        var report=denuncias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Denuncia no encontrada"));
        if("LUGAR".equals(report.getTipoContenido())) lugares.deleteById(report.getIdContenido());
        else if("RESENA".equals(report.getTipoContenido())) resenas.deleteById(report.getIdContenido());
        else reportes.deleteById(report.getIdContenido());
        report.setEstado("CONTENIDO_ELIMINADO"); denuncias.save(report);
    }
    @PutMapping("/denuncias/{id}/descartar")
    public ReporteContenidoResponse descartar(@RequestHeader("Authorization") String auth,@PathVariable Long id) {
        requireAdmin(auth);
        var report=denuncias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Denuncia no encontrada"));
        report.setEstado("DESCARTADO"); return contenidoDenunciado(denuncias.save(report));
    }
    @GetMapping("/dashboard")
    public Map<String,Long> dashboard(@RequestHeader("Authorization") String auth) {
        requireAdmin(auth); Map<String,Long> result=new LinkedHashMap<>();
        result.put("lugares",lugares.count()); result.put("resenas",resenas.count());
        result.put("reportesSeguridad",reportes.count()); result.put("denunciasPendientes",
                (long)denuncias.findByEstadoOrderByFechaCreacionAsc("PENDIENTE").size()); return result;
    }
    @DeleteMapping("/lugares/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarLugar(@RequestHeader("Authorization") String auth,@PathVariable Long id) {
        requireAdmin(auth); if(!lugares.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Lugar no encontrado"); lugares.deleteById(id);
    }
    @GetMapping("/lugares")
    public List<LugarResponse> lugares(@RequestHeader("Authorization") String auth) {
        requireAdmin(auth); return lugares.findAll(PageRequest.of(0,200)).stream().map(LugarResponse::desde).toList();
    }
    @GetMapping("/reportes")
    public List<ReporteResponse> reportes(@RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue="false") boolean caducados) {
        requireAdmin(auth);
        var values=caducados?reportes.listarCaducados(PageRequest.of(0,200))
                :reportes.listarActivos(PageRequest.of(0,200));
        return values.stream().map(ReporteResponse::desde).toList();
    }
    @DeleteMapping("/reportes/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarReporte(@RequestHeader("Authorization") String auth,@PathVariable Long id) {
        requireAdmin(auth); if(!reportes.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Reporte no encontrado"); reportes.deleteById(id);
    }
    private void requireAdmin(String auth) {
        if(!"ADMIN".equalsIgnoreCase(sesiones.obtenerUsuario(auth).getRol()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Se requiere una cuenta administradora");
    }
    private ReporteContenidoResponse contenidoDenunciado(ReporteContenido denuncia) {
        String tipo=denuncia.getTipoContenido(); Long id=denuncia.getIdContenido();
        if("LUGAR".equals(tipo)) return lugares.findById(id)
                .map(lugar->ReporteContenidoResponse.desde(denuncia,
                        lugar.getCategoria()==null?lugar.getTipo():lugar.getCategoria().getNombre(),
                        lugar.getNombre()))
                .orElseGet(()->ReporteContenidoResponse.desde(denuncia,"LUGAR","Contenido eliminado"));
        if("RESENA".equals(tipo)) return resenas.findWithLugarById(id)
                .map(resena->{var lugar=resena.getLugar();String categoria=lugar.getCategoria()==null
                        ?lugar.getTipo():lugar.getCategoria().getNombre();
                    return ReporteContenidoResponse.desde(denuncia,"RESEÑA · "+categoria,
                            "Reseña de "+lugar.getNombre());})
                .orElseGet(()->ReporteContenidoResponse.desde(denuncia,"RESEÑA","Contenido eliminado"));
        if("REPORTE".equals(tipo)) return reportes.findById(id)
                .map(reporte->ReporteContenidoResponse.desde(denuncia,
                        "ALTO".equals(reporte.getNivelPeligro())?"PELIGRO ALTO":"PELIGRO MEDIO",
                        reporte.getTipoEvento()))
                .orElseGet(()->ReporteContenidoResponse.desde(denuncia,"REPORTE","Contenido eliminado"));
        return ReporteContenidoResponse.desde(denuncia,tipo,"Contenido #"+id);
    }
}
