package api_app_mapas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 50) String primerNombre,
        @Size(max = 50) String segundoNombre,
        @NotBlank @Size(max = 50) String apellidoPaterno,
        @Size(max = 50) String apellidoMaterno,
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String confirmarPassword
) {
}
