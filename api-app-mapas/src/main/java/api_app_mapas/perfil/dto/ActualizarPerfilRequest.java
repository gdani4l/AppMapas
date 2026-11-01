package api_app_mapas.perfil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ActualizarPerfilRequest(
        @NotBlank @Size(max = 50) String primerNombre,
        @Size(max = 50) String segundoNombre,
        @NotBlank @Size(max = 50) String apellidoPaterno,
        @Size(max = 50) String apellidoMaterno,
        @NotBlank @Email @Size(max = 254) String correo,
        @Past LocalDate fechaNacimiento,
        @Size(max = 100) String distrito,
        @Pattern(regexp = "^[0-9+() -]{0,20}$", message = "El teléfono contiene caracteres no válidos")
        String telefono,
        @Size(max = 255) String direccion
) {
}
