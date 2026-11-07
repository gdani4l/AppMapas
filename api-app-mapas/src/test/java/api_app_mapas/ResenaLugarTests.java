package api_app_mapas;

import api_app_mapas.lugares.controller.LugarController;
import api_app_mapas.lugares.controller.ResenaLugarController;
import api_app_mapas.lugares.dto.LugarRequest;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ResenaLugarTests {
    @Autowired LugarController lugares;
    @Autowired ResenaLugarController resenas;
    @Autowired SesionService sesiones;
    @Autowired UsuarioRepository usuarios;
    @Autowired ResenaLugarRepository resenaRepository;

    @Test
    void creaEditaListaYEliminaResenaSinDejarDatos() throws Exception {
        var usuario = usuarios.findAll().stream().findFirst().orElseThrow();
        String bearer = "Bearer " + sesiones.crearSesion(usuario);
        long lugarId = lugares.crear(bearer, new LugarRequest("HOTEL", "Prueba de reseñas",
                "Prueba", "Calle de prueba", "Cercado de Lima", new BigDecimal("-12.0464000"),
                new BigDecimal("-77.0428000"))).idLugar();

        assertEquals(0, resenas.listar(lugarId, 0, 3).total());
        assertTrue(resenas.imagenes(lugarId).isEmpty());
        assertTrue(resenas.fotos(lugarId).isEmpty());
        assertNull(resenas.mia(lugarId, bearer).getBody());
        var primera = resenas.crear(lugarId, bearer, 0, "Experiencia breve", null);
        var conFoto = resenaRepository.findById(primera.idResena()).orElseThrow();
        conFoto.setImagenUrl("https://example.com/foto.jpg");
        resenaRepository.saveAndFlush(conFoto);
        assertEquals(java.util.List.of("https://example.com/foto.jpg"), resenas.imagenes(lugarId));
        var fotos = resenas.fotos(lugarId);
        assertEquals(1, fotos.size());
        assertEquals("https://example.com/foto.jpg", fotos.get(0).imagenUrl());
        assertEquals(primera.nombreUsuario(), fotos.get(0).nombreUsuario());
        assertEquals(usuario.getImagenPerfilUrl(), fotos.get(0).imagenPerfilUrl());
        assertEquals(0, primera.estrellas());
        assertEquals(usuario.getPrimerNombre() + " " + usuario.getApellidoPaterno(),
                primera.nombreUsuario());
        assertEquals(1, resenas.listar(lugarId, 0, 3).total());
        assertEquals(0, resenas.listar(lugarId, 0, 3).promedio());
        assertThrows(ResponseStatusException.class,
                () -> resenas.crear(lugarId, bearer, 6, "Inválido", null));
        assertThrows(ResponseStatusException.class,
                () -> resenas.crear(lugarId, bearer, 3, "Duplicado", null));

        var editada = resenas.editar(lugarId, bearer, 5, "Muy buen lugar", null, false);
        assertEquals(5, editada.estrellas());
        assertEquals(5, resenas.listar(lugarId, 0, 3).promedio());
        assertNotNull(resenas.mia(lugarId, bearer).getBody());
        resenas.eliminar(lugarId, bearer);
        assertEquals(0, resenas.listar(lugarId, 0, 3).total());
    }
}
