package api_app_mapas.recuperacion.service;

import api_app_mapas.recuperacion.dto.MensajeResponse;
import api_app_mapas.recuperacion.dto.RestablecerPasswordRequest;
import api_app_mapas.recuperacion.dto.SolicitudRecuperacionRequest;
import api_app_mapas.recuperacion.model.CodigoRecuperacion;
import api_app_mapas.recuperacion.repository.CodigoRecuperacionRepository;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class RecuperacionPasswordService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacionRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String correoRemitente;
    private final int minutosExpiracion;

    public RecuperacionPasswordService(
            UsuarioRepository usuarioRepository,
            CodigoRecuperacionRepository codigoRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String correoRemitente,
            @Value("${app.recuperacion.minutos-expiracion:15}") int minutosExpiracion
    ) {
        this.usuarioRepository = usuarioRepository;
        this.codigoRepository = codigoRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.correoRemitente = correoRemitente;
        this.minutosExpiracion = minutosExpiracion;
    }

    @Transactional
    public MensajeResponse solicitarCodigo(SolicitudRecuperacionRequest request) {
        Optional<Usuario> usuarioOpcional = usuarioRepository.findByCorreo(
                normalizarCorreo(request.correo())
        );

        if (usuarioOpcional.isEmpty()) {
            return respuestaSolicitud();
        }

        Usuario usuario = usuarioOpcional.get();
        String codigoPlano = generarCodigo();

        codigoRepository.invalidarCodigosActivos(usuario);
        codigoRepository.save(new CodigoRecuperacion(
                usuario,
                calcularHash(codigoPlano),
                LocalDateTime.now().plusMinutes(minutosExpiracion)
        ));

        enviarCorreo(usuario, codigoPlano);
        return respuestaSolicitud();
    }

    @Transactional
    public MensajeResponse restablecerPassword(RestablecerPasswordRequest request) {
        if (!request.nuevaPassword().equals(request.confirmarPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        Usuario usuario = usuarioRepository.findByCorreo(normalizarCorreo(request.correo()))
                .orElseThrow(this::codigoInvalido);

        CodigoRecuperacion codigo = codigoRepository
                .findFirstByUsuarioAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                        usuario,
                        LocalDateTime.now()
                )
                .orElseThrow(this::codigoInvalido);

        if (!calcularHash(request.codigo()).equals(codigo.getCodigoHash())) {
            throw codigoInvalido();
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()));
        codigo.setUtilizado(true);

        return new MensajeResponse(true, "La contraseña se cambió correctamente");
    }

    private MensajeResponse respuestaSolicitud() {
        return new MensajeResponse(
                true,
                "Si el correo está registrado, recibirás un código de verificación."
        );
    }

    private String generarCodigo() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String calcularHash(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el hash del código", e);
        }
    }

    private void enviarCorreo(Usuario usuario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(correoRemitente);
        mensaje.setTo(usuario.getCorreo());
        mensaje.setSubject("Código de recuperación - App de mapas");
        mensaje.setText("""
                Hola %s:

                Tu código de recuperación es: %s

                El código vence en %d minutos. Si no solicitaste este cambio,
                puedes ignorar este correo.
                """.formatted(usuario.getPrimerNombre(), codigo, minutosExpiracion));

        mailSender.send(mensaje);
    }

    private ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El código de recuperación no es válido o venció"
        );
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
