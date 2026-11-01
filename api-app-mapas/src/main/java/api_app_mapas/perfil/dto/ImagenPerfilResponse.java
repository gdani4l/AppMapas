package api_app_mapas.perfil.dto;

public record ImagenPerfilResponse(
        boolean exito,
        String mensaje,
        String imagenPerfilUrl
) {
}
