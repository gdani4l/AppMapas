package api_app_mapas.reportes.dto;
import jakarta.validation.constraints.Pattern;
public record VotoRequest(@Pattern(regexp="VERDADERO|FALSO") String veracidad) {}
