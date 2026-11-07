package api_app_mapas.reportes.dto;

import api_app_mapas.reportes.model.ComentarioReporte;
import api_app_mapas.usuario.model.Usuario;
import java.time.LocalDateTime;

public record ComentarioReporteResponse(Long id, String autor, String imagenAutor, String comentario,
                                        String imagenUrl, LocalDateTime fechaCreacion) {
    public static ComentarioReporteResponse desde(ComentarioReporte c) {
        Usuario u = c.getUsuario();
        String autor = (u.getPrimerNombre() + " " + u.getApellidoPaterno()).trim();
        return new ComentarioReporteResponse(c.getIdComentario(), autor, u.getImagenPerfilUrl(),
                c.getComentario(), c.getImagenUrl(), c.getFechaCreacion());
    }
}
