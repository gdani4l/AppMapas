package api_app_mapas;

import api_app_mapas.lugares.controller.LugarController;
import api_app_mapas.lugares.dto.LugarRequest;
import api_app_mapas.lugares.repository.CategoriaLugarRepository;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LugarControllerTests {
    @Autowired LugarController controller;
    @Autowired SesionService sesiones;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaLugarRepository categorias;

    @Test
    void registraBuscaFiltraYMuestraDetalleSinDejarDatosDePrueba() {
        assertTrue(categorias.existsByCodigoAndActivaTrue("HOTEL"));
        assertTrue(controller.categorias().stream()
                .anyMatch(item -> item.codigo().equals("PARQUE")));
        var usuario = usuarios.findAll().stream().findFirst().orElseThrow();
        String token = sesiones.crearSesion(usuario);
        var lugar = controller.crear("Bearer " + token,
                new LugarRequest("HOTEL", "Hotel de prueba HU23", "Descripción breve",
                        "Av. de prueba 123", new BigDecimal("-12.0464000"),
                        new BigDecimal("-77.0428000")));
        assertNotNull(lugar.idLugar());
        assertEquals(lugar.idLugar(), controller.detalle(lugar.idLugar()).idLugar());
        assertTrue(controller.buscar("Hotel de prueba HU23", new BigDecimal("-12.0464000"),
                new BigDecimal("-77.0428000")).stream()
                .anyMatch(item -> item.idLugar().equals(lugar.idLugar())));
        assertTrue(controller.listar(new BigDecimal("-12.1"), new BigDecimal("-12.0"),
                new BigDecimal("-77.1"), new BigDecimal("-77.0"), "HOTEL", null).stream()
                .anyMatch(item -> item.idLugar().equals(lugar.idLugar())));
        assertTrue(controller.listar(new BigDecimal("-12.1"), new BigDecimal("-12.0"),
                new BigDecimal("-77.1"), new BigDecimal("-77.0"), "RESTAURANTE", null).isEmpty());
        assertTrue(controller.listar(new BigDecimal("-12.1"), new BigDecimal("-12.0"),
                new BigDecimal("-77.1"), new BigDecimal("-77.0"), null,
                List.of("HOTEL", "RESTAURANTE")).stream()
                .anyMatch(item -> item.idLugar().equals(lugar.idLugar())));
    }
}
