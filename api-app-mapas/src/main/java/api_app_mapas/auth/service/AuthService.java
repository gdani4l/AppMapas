package api_app_mapas.auth.service;

import api_app_mapas.auth.dto.AuthResponse;
import api_app_mapas.auth.dto.LoginRequest;
import api_app_mapas.auth.dto.RegistroRequest;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SesionService sesionService;
    private final VerificacionAccesoService verificacion;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       SesionService sesionService, VerificacionAccesoService verificacion) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.sesionService = sesionService;
        this.verificacion = verificacion;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String correo = normalizarCorreo(request.correo());

        if (!request.password().equals(request.confirmarPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una cuenta con este correo. Inicia sesión para continuar o verificarla.");
        }

        Usuario usuario = new Usuario();
        usuario.setPrimerNombre(request.primerNombre().trim());
        usuario.setSegundoNombre(limpiarOpcional(request.segundoNombre()));
        usuario.setApellidoPaterno(request.apellidoPaterno().trim());
        usuario.setApellidoMaterno(limpiarOpcional(request.apellidoMaterno()));
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));

        Usuario guardado = usuarioRepository.save(usuario);
        return verificacion.iniciar(guardado);
    }

    @Transactional
    public AuthResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository.bloquearPorCorreo(normalizarCorreo(request.correo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        return verificacion.iniciar(usuario);
    }

    private AuthResponse respuesta(Usuario usuario, String mensaje, String tokenSesion) {
        String segundoNombre = usuario.getSegundoNombre() == null ? "" : " " + usuario.getSegundoNombre();
        return new AuthResponse(
                true,
                mensaje,
                usuario.getIdUsuario(),
                usuario.getPrimerNombre() + segundoNombre + " " + usuario.getApellidoPaterno(),
                tokenSesion, null, "COMPLETO"
        );
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
