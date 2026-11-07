package api_app_mapas.lugares.service;

import api_app_mapas.lugares.model.Lugar;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LugarUsoService {
    private final JdbcTemplate jdbc;

    public LugarUsoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void registrarLugar(String tipoEvento, Lugar lugar) {
        jdbc.update("INSERT INTO eventos_uso_lugar "
                        + "(tipo_evento,id_lugar,nombre_lugar,categoria) VALUES (?,?,?,?)",
                tipoEvento, lugar.getIdLugar(), lugar.getNombre(), lugar.getTipo());
    }

}
