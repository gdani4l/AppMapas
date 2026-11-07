package api_app_mapas.reportes.dto;

import api_app_mapas.reportes.model.ReporteContenido;
import java.time.LocalDateTime;

public record ReporteContenidoResponse(Long id, String tipoContenido, Long idContenido,
                                       String categoriaContenido, String nombreContenido,
                                       String motivo, String estado, String usuario,
                                       LocalDateTime fechaCreacion) {
    public static ReporteContenidoResponse desde(ReporteContenido value) {
        return desde(value, value.getTipoContenido(), "Contenido #" + value.getIdContenido());
    }
    public static ReporteContenidoResponse desde(ReporteContenido value,String categoria,String contenido) {
        String nombre = value.getUsuario().getPrimerNombre() + " "
                + value.getUsuario().getApellidoPaterno();
        return new ReporteContenidoResponse(value.getId(), value.getTipoContenido(),
                value.getIdContenido(), categoria, contenido, value.getMotivo(), value.getEstado(), nombre.trim(),
                value.getFechaCreacion());
    }
}
