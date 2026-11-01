package api_app_mapas;

import api_app_mapas.perfil.service.PerfilService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PerfilLecturaTests {
    @Autowired PerfilService perfiles;
    @Autowired UsuarioRepository usuarios;
    @Autowired EntityManagerFactory factory;

    @Test
    void cargaDatosRegistradosAunqueLaReferenciaDeSesionEsteDesconectada() {
        var existentes = usuarios.findAll();
        assertFalse(existentes.isEmpty(), "La prueba requiere una cuenta registrada local");
        for (Usuario esperado : existentes) {
            var em = factory.createEntityManager();
            Usuario referencia = em.getReference(Usuario.class, esperado.getIdUsuario());
            em.close();
            var perfil = perfiles.obtener(referencia);
            assertEquals(esperado.getPrimerNombre(), perfil.primerNombre());
            assertEquals(esperado.getCorreo(), perfil.correo());
            assertEquals(esperado.getImagenPerfilUrl(), perfil.imagenPerfilUrl());
        }
    }
}
