package api_app_mapas.perfil.dto;

import java.time.LocalDate;

public record PerfilResponse(
        Integer idUsuario,
        String primerNombre,
        String segundoNombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        LocalDate fechaNacimiento,
        String distrito,
        String telefono,
        String direccion,
        String imagenPerfilUrl
) {
}
