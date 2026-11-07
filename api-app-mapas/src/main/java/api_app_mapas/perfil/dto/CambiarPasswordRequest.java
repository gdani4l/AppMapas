package api_app_mapas.perfil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank @Size(min = 8, max = 72,
                message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
        String nuevaPassword,
        @NotBlank String confirmarPassword
) {
}
