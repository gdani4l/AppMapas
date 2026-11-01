package api_app_mapas.auth.dto;

public record AuthResponse(
        boolean exito,
        String mensaje,
        Integer idUsuario,
        String nombreCompleto,
        String tokenSesion
) {
}
