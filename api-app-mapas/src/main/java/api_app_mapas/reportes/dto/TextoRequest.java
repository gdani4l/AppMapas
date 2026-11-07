package api_app_mapas.reportes.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record TextoRequest(@NotBlank @Size(max=1000) String texto) {}
