package api_app_mapas.recuperacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordRequest(
        @NotBlank @Email String correo,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos") String codigo,
        @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String nuevaPassword,
        @NotBlank String confirmarPassword
) {
}
