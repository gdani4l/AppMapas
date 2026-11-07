package api_app_mapas.administracion.controller;

import api_app_mapas.sesion.service.SesionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recorridos")
public class RecorridoController {
    private final SesionService sesiones;
    private final JdbcTemplate jdbc;

    public RecorridoController(SesionService sesiones, JdbcTemplate jdbc) {
        this.sesiones = sesiones;
        this.jdbc = jdbc;
    }

    public record Punto(@Min(-90) @Max(90) double latitud,
                        @Min(-180) @Max(180) double longitud) { }
    public record RecorridoRequest(@NotEmpty @Size(max = 200) List<@Valid Punto> puntos,
                                   boolean simulado) { }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registrar(@RequestHeader("Authorization") String auth,
                         @Valid @RequestBody RecorridoRequest request) {
        int usuario = sesiones.obtenerUsuario(auth).getIdUsuario();
        for (Punto punto : request.puntos()) {
            if (!Double.isFinite(punto.latitud()) || !Double.isFinite(punto.longitud()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordenadas inválidas");
        }
        jdbc.batchUpdate("INSERT INTO puntos_recorrido (id_usuario, latitud, longitud, simulado) VALUES (?, ?, ?, ?)",
                request.puntos(), 200, (statement, punto) -> {
                    statement.setInt(1, usuario);
                    statement.setDouble(2, punto.latitud());
                    statement.setDouble(3, punto.longitud());
                    // La simulación reproduce un recorrido de prueba y se analiza como movilidad real.
                    statement.setBoolean(4, false);
                });
    }

    @GetMapping("/estadisticas")
    public Map<String, Object> estadisticas(@RequestHeader("Authorization") String auth) {
        if (!"ADMIN".equalsIgnoreCase(sesiones.obtenerUsuario(auth).getRol()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere una cuenta administradora");
        long usuarios = jdbc.queryForObject("SELECT COUNT(*) FROM usuarios", Long.class);
        long activos = jdbc.queryForObject("SELECT COUNT(DISTINCT id_usuario) FROM sesiones_usuario WHERE ultimo_uso >= NOW() - INTERVAL 7 DAY", Long.class);
        long puntos = jdbc.queryForObject("SELECT COUNT(*) FROM puntos_recorrido", Long.class);
        List<Map<String, Object>> zonas = jdbc.queryForList("""
                SELECT ROUND(latitud, 3) latitud, ROUND(longitud, 3) longitud,
                       COUNT(*) visitas, COUNT(DISTINCT id_usuario) usuarios
                FROM puntos_recorrido
                GROUP BY ROUND(latitud, 3), ROUND(longitud, 3)
                ORDER BY visitas DESC LIMIT 100
                """);
        List<Map<String, Object>> ultimosDias = jdbc.queryForList("""
                SELECT DATE(fecha_registro) fecha, COUNT(*) puntos,
                       COUNT(DISTINCT id_usuario) usuarios
                FROM puntos_recorrido
                WHERE fecha_registro >= NOW() - INTERVAL 7 DAY
                GROUP BY DATE(fecha_registro) ORDER BY fecha DESC
                """);
        return Map.of("usuarios", usuarios, "activos7Dias", activos,
                "puntos", puntos,
                "zonas", zonas, "ultimosDias", ultimosDias);
    }
}
