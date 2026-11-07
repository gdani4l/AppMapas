package api_app_mapas.administracion.controller;

import api_app_mapas.sesion.service.SesionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bi")
public class BusinessIntelligenceController {
    private final JdbcTemplate jdbc;
    private final SesionService sesiones;
    public BusinessIntelligenceController(JdbcTemplate jdbc, SesionService sesiones) {
        this.jdbc = jdbc; this.sesiones = sesiones;
    }

    @GetMapping
    public Map<String, Object> indicadores(@RequestHeader("Authorization") String auth) {
        if (!"ADMIN".equalsIgnoreCase(sesiones.obtenerUsuario(auth).getRol()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere una cuenta administradora");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("registros", jdbc.queryForList("SELECT DATE(fecha_registro) etiqueta, COUNT(*) valor FROM usuarios WHERE fecha_registro >= NOW() - INTERVAL 30 DAY GROUP BY DATE(fecha_registro) ORDER BY etiqueta"));
        result.put("distritos", jdbc.queryForList("SELECT COALESCE(NULLIF(TRIM(distrito), ''), 'Sin indicar') etiqueta, COUNT(*) valor FROM usuarios GROUP BY etiqueta ORDER BY valor DESC LIMIT 15"));
        result.put("valoraciones", jdbc.queryForList("SELECT CONCAT(estrellas, ' estrellas') etiqueta, COUNT(*) valor FROM resenas_lugar GROUP BY estrellas ORDER BY estrellas"));
        result.put("incidentes", jdbc.queryForList("SELECT tipo_evento etiqueta, COUNT(*) valor FROM reportes_seguridad GROUP BY tipo_evento ORDER BY valor DESC"));
        result.put("reportesDistritos", jdbc.queryForList("SELECT COALESCE(NULLIF(TRIM(distrito), ''), 'Sin identificar') etiqueta, COUNT(*) valor FROM reportes_seguridad GROUP BY etiqueta ORDER BY valor DESC LIMIT 15"));
        result.put("reportesHoras", jdbc.queryForList("SELECT CONCAT(LPAD(HOUR(fecha_creacion),2,'0'), ':00') etiqueta, COUNT(*) valor FROM reportes_seguridad GROUP BY etiqueta ORDER BY etiqueta"));
        result.put("reportesDias", jdbc.queryForList("SELECT ELT(DAYOFWEEK(fecha_creacion),'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') etiqueta, COUNT(*) valor FROM reportes_seguridad GROUP BY etiqueta ORDER BY MIN(DAYOFWEEK(fecha_creacion))"));
        result.put("incidentesDistrito", jdbc.queryForList("WITH conteo AS (SELECT COALESCE(NULLIF(TRIM(distrito),''),'Sin identificar') distrito,tipo_evento,COUNT(*) valor FROM reportes_seguridad GROUP BY distrito,tipo_evento), ordenados AS (SELECT distrito,tipo_evento,valor,ROW_NUMBER() OVER (PARTITION BY distrito ORDER BY valor DESC,tipo_evento) posicion FROM conteo) SELECT CONCAT(distrito,' · ',tipo_evento) etiqueta,valor FROM ordenados WHERE posicion=1 ORDER BY valor DESC,distrito"));
        result.put("validacion", jdbc.queryForList("SELECT CASE WHEN estado_validacion='CONFIRMADO' THEN 'Confirmados' WHEN estado_validacion='REFUTADO' THEN 'Refutados' ELSE 'Pendientes' END etiqueta, ROUND(COUNT(*)*100.0/(SELECT COUNT(*) FROM reportes_seguridad)) valor FROM reportes_seguridad GROUP BY etiqueta ORDER BY valor DESC"));
        result.put("zonasAlertas", jdbc.queryForList("SELECT COALESCE(NULLIF(TRIM(r.distrito),''),'Sin identificar') etiqueta,COUNT(*) valor FROM eventos_alerta_usuario e JOIN reportes_seguridad r ON r.id_reporte=e.id_reporte GROUP BY etiqueta ORDER BY valor DESC"));
        result.put("zonasReincidentes", jdbc.queryForList("SELECT distrito etiqueta,SUM(cantidad) valor FROM (SELECT COALESCE(NULLIF(TRIM(distrito),''),'Sin identificar') distrito,ROUND(latitud,3) latitud,ROUND(longitud,3) longitud,COUNT(*) cantidad FROM reportes_seguridad GROUP BY distrito,latitud,longitud HAVING COUNT(*)>1) zonas GROUP BY distrito ORDER BY valor DESC"));
        result.put("actividadDistritos", jdbc.queryForList("SELECT etiqueta,SUM(valor) valor FROM (SELECT COALESCE(NULLIF(TRIM(r.distrito),''),'Sin identificar') etiqueta,COUNT(*) valor FROM reportes_seguridad r GROUP BY etiqueta UNION ALL SELECT COALESCE(NULLIF(TRIM(r.distrito),''),'Sin identificar') etiqueta,COUNT(*) valor FROM comentarios_reporte c JOIN reportes_seguridad r ON r.id_reporte=c.id_reporte GROUP BY etiqueta UNION ALL SELECT COALESCE(NULLIF(TRIM(r.distrito),''),'Sin identificar') etiqueta,COUNT(*) valor FROM votos_reporte v JOIN reportes_seguridad r ON r.id_reporte=v.id_reporte GROUP BY etiqueta) actividad GROUP BY etiqueta ORDER BY valor DESC LIMIT 15"));
        return result;
    }
}
