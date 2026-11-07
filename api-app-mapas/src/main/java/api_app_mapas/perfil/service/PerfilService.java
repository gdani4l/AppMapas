package api_app_mapas.perfil.service;

import api_app_mapas.perfil.dto.ActualizarPerfilRequest;
import api_app_mapas.perfil.dto.CambiarPasswordRequest;
import api_app_mapas.perfil.dto.PerfilResponse;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public PerfilService(
            UsuarioRepository usuarioRepository,
            CloudinaryService cloudinaryService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cambiarPassword(Usuario usuario, CambiarPasswordRequest request) {
        Usuario actual = cargarUsuario(usuario);
        if (!passwordEncoder.matches(request.passwordActual(), actual.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña actual no es correcta");
        }
        if (!request.nuevaPassword().equals(request.confirmarPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las contraseñas nuevas no coinciden");
        }
        if (passwordEncoder.matches(request.nuevaPassword(), actual.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe ser diferente a la actual");
        }
        actual.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()));
        usuarioRepository.save(actual);
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtener(Usuario usuario) {
        return respuesta(cargarUsuario(usuario));
    }

    @Transactional
    public PerfilResponse actualizar(Usuario usuario, ActualizarPerfilRequest request) {
        Usuario actual = cargarUsuario(usuario);
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (!correo.equals(actual.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El correo verificado de acceso no se puede cambiar desde la edición de perfil.");
        }
        usuarioRepository.findByCorreo(correo)
                .filter(otro -> !otro.getIdUsuario().equals(usuario.getIdUsuario()))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe una cuenta con este correo"
                    );
                });

        actual.setPrimerNombre(request.primerNombre().trim());
        actual.setSegundoNombre(limpiar(request.segundoNombre()));
        actual.setApellidoPaterno(request.apellidoPaterno().trim());
        actual.setApellidoMaterno(limpiar(request.apellidoMaterno()));
        actual.setCorreo(correo);
        

        Usuario perfil = actual;
        perfil.setFechaNacimiento(request.fechaNacimiento());
        perfil.setDistrito(limpiar(request.distrito()));
        perfil.setTelefono(limpiar(request.telefono()));
        perfil.setDireccion(limpiar(request.direccion()));
        usuarioRepository.save(perfil);

        return respuesta(perfil);
    }

    @Transactional
    public String actualizarImagen(Usuario usuario, byte[] contenido) {
        Usuario actual = cargarUsuario(usuario);
        CloudinaryService.UploadResult resultado =
                cloudinaryService.subirImagenPerfil(contenido, usuario.getIdUsuario());

        Usuario perfil = actual;
        perfil.setImagenPerfilUrl(resultado.url());
        perfil.setImagenPublicId(resultado.publicId());
        usuarioRepository.save(perfil);
        return resultado.url();
    }

    private Usuario cargarUsuario(Usuario referencia) {
        return usuarioRepository.findById(referencia.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private PerfilResponse respuesta(Usuario usuario) {
        return new PerfilResponse(
                usuario.getIdUsuario(),
                usuario.getPrimerNombre(),
                usuario.getSegundoNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                usuario.getCorreo(),
                usuario.getFechaNacimiento(),
                usuario.getDistrito(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getImagenPerfilUrl(),
                usuario.getRol()
        );
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
