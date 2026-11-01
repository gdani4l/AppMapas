package api_app_mapas.lugares.dto;

import api_app_mapas.lugares.model.ResenaLugar;
import api_app_mapas.usuario.model.Usuario;
import java.time.LocalDateTime;

public record ResenaResponse(Long idResena, Integer idUsuario, String nombreUsuario,
                             String imagenPerfilUrl, int estrellas, String comentario,
                             String imagenUrl, LocalDateTime fechaCreacion,
                             LocalDateTime fechaActualizacion) {
    public static ResenaResponse desde(ResenaLugar resena) {
        Usuario usuario = resena.getUsuario();
        return new ResenaResponse(resena.getId(), usuario.getIdUsuario(),
                usuario.getPrimerNombre() + " " + usuario.getApellidoPaterno(),
                usuario.getImagenPerfilUrl(), resena.getEstrellas(), resena.getComentario(),
                resena.getImagenUrl(), resena.getFechaCreacion(), resena.getFechaActualizacion());
    }
}
