package api_app_mapas;

import api_app_mapas.lugares.model.Lugar;
import api_app_mapas.lugares.model.ResenaLugar;
import api_app_mapas.lugares.repository.LugarRepository;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.lugares.service.ResenaLugarService;
import api_app_mapas.perfil.service.CloudinaryService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResenaUsuarioDesacopladoTests {
    @Test
    void creaRespuestaSinLeerCamposDelUsuarioDesacoplado() throws Exception {
        var resenas = mock(ResenaLugarRepository.class);
        var lugares = mock(LugarRepository.class);
        var cloudinary = mock(CloudinaryService.class);
        var usuarios = mock(UsuarioRepository.class);
        var usuarioDesacoplado = mock(Usuario.class);
        when(usuarioDesacoplado.getIdUsuario()).thenReturn(7);
        when(usuarioDesacoplado.getPrimerNombre()).thenThrow(
                new IllegalStateException("No debe leer el proxy desacoplado"));
        Usuario autor = new Usuario();
        autor.setIdUsuario(7);
        autor.setPrimerNombre("Daniel");
        autor.setApellidoPaterno("Pérez");
        when(usuarios.findById(7)).thenReturn(Optional.of(autor));
        when(lugares.findById(4L)).thenReturn(Optional.of(new Lugar()));
        when(resenas.findByLugarIdLugarAndUsuarioIdUsuario(4L, 7)).thenReturn(Optional.empty());
        when(resenas.saveAndFlush(any(ResenaLugar.class))).thenAnswer(call -> call.getArgument(0));

        var resultado = new ResenaLugarService(resenas, lugares, cloudinary, usuarios)
                .guardar(4L, usuarioDesacoplado, 5, "Buen lugar", null, false, false);

        assertEquals("Daniel Pérez", resultado.nombreUsuario());
        assertEquals(5, resultado.estrellas());
        verify(usuarioDesacoplado, never()).getPrimerNombre();
    }
}
