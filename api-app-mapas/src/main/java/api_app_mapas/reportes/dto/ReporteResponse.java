package api_app_mapas.reportes.dto;

import api_app_mapas.reportes.model.ReporteSeguridad;
import api_app_mapas.usuario.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteResponse(Long id, String tipoEvento, String nivelPeligro, String descripcion,
                              BigDecimal latitud, BigDecimal longitud, Integer radioMetros,
                              boolean anonimo, String autor, String imagenAutor, String estadoValidacion,
                              Integer confirmaciones, Integer refutaciones, LocalDateTime fechaCreacion,
                              LocalDateTime fechaExpiracion, String distrito, String imagenUrl, boolean propio) {
    public static ReporteResponse desde(ReporteSeguridad r) { return desde(r, false); }
    public static ReporteResponse desde(ReporteSeguridad r, boolean propio) {
        Usuario u = r.getUsuario();
        String autor = r.isAnonimo() || u == null ? "Anónimo" : (u.getPrimerNombre() + " " + u.getApellidoPaterno()).trim();
        String imagen = r.isAnonimo() || u == null ? null : u.getImagenPerfilUrl();
        return new ReporteResponse(r.getIdReporte(), r.getTipoEvento(), r.getNivelPeligro(), r.getDescripcion(),
                r.getLatitud(), r.getLongitud(), r.getRadioMetros(), r.isAnonimo(), autor, imagen,
                r.getEstadoValidacion(), r.getConfirmaciones(), r.getRefutaciones(), r.getFechaCreacion(),
                r.getFechaExpiracion(), r.getDistrito(), r.getImagenUrl(), propio);
    }
}
