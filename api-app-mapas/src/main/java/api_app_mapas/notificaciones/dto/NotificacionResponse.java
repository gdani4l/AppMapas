package api_app_mapas.notificaciones.dto;

import api_app_mapas.notificaciones.model.Notificacion;
import java.time.LocalDateTime;

public record NotificacionResponse(Long id, String tipo, String titulo, String mensaje,
                                   Long idReferencia, boolean leida, LocalDateTime fechaCreacion) {
    public static NotificacionResponse desde(Notificacion value) {
        return new NotificacionResponse(value.getId(), value.getTipo(), value.getTitulo(),
                value.getMensaje(), value.getIdReferencia(), value.isLeida(), value.getFechaCreacion());
    }
}
