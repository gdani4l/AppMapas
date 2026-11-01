package api_app_mapas.sesion.service;

import api_app_mapas.sesion.model.SesionUsuario;
import api_app_mapas.sesion.repository.SesionUsuarioRepository;
import api_app_mapas.usuario.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SesionService {

    private final SesionUsuarioRepository sesionRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long diasExpiracion;

    public SesionService(
            SesionUsuarioRepository sesionRepository,
            @Value("${app.sesion.dias-expiracion:30}") long diasExpiracion
    ) {
        this.sesionRepository = sesionRepository;
        this.diasExpiracion = diasExpiracion;
    }

    @Transactional
    public String crearSesion(Usuario usuario) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        SesionUsuario sesion = new SesionUsuario();
        sesion.setUsuario(usuario);
        sesion.setTokenHash(hash(token));
        sesion.setFechaExpiracion(LocalDateTime.now().plusDays(diasExpiracion));
        sesion.setRevocada(false);
        sesionRepository.save(sesion);
        return token;
    }

    @Transactional
    public Usuario obtenerUsuario(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida");
        }

        SesionUsuario sesion = sesionRepository
                .findByTokenHashAndRevocadaFalse(hash(token))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Sesión no válida"));

        if (sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            sesion.setRevocada(true);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión expiró");
        }

        sesion.setUltimoUso(LocalDateTime.now());
        return sesion.getUsuario();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}
