package api_app_mapas.auth.service;

import api_app_mapas.auth.dto.AuthResponse;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class VerificacionAccesoService {
    private final JdbcTemplate jdbc;
    private final UsuarioRepository usuarios;
    private final SesionService sesiones;
    private final PasswordEncoder encoder;
    private final JavaMailSender mail;
    private final String remitente;
    private final SecureRandom random = new SecureRandom();

    public VerificacionAccesoService(JdbcTemplate jdbc, UsuarioRepository usuarios, SesionService sesiones,
                                    PasswordEncoder encoder, JavaMailSender mail,
                                    @Value("${spring.mail.username}") String remitente) {
        this.jdbc=jdbc; this.usuarios=usuarios; this.sesiones=sesiones;
        this.encoder=encoder; this.mail=mail; this.remitente=remitente;
    }

    @Transactional
    public AuthResponse iniciar(Usuario referencia) {
        Usuario usuario = usuarios.bloquearPorId(referencia.getIdUsuario()).orElseThrow(this::invalido);
        if (!usuario.isActiva()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta suspendida. Contacta al administrador.");
        LocalDateTime now=LocalDateTime.now();
        Long recientes=jdbc.queryForObject("SELECT COUNT(*) FROM desafios_acceso WHERE id_usuario=? AND enviado>?",
                Long.class, usuario.getIdUsuario(), now.minusSeconds(60));
        Long hora=jdbc.queryForObject("SELECT COUNT(*) FROM desafios_acceso WHERE id_usuario=? AND creado>?",
                Long.class, usuario.getIdUsuario(), now.minusHours(1));
        if (recientes>0 || hora>=10) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Espera antes de solicitar otro código. Revisa también la carpeta de spam.");
        String id=UUID.randomUUID().toString(), code=codigo();
        String purpose=usuario.isCorreoVerificado()?"LOGIN":"CORREO";
        jdbc.update("UPDATE desafios_acceso SET usado=1 WHERE id_usuario=? AND usado=0", usuario.getIdUsuario());
        jdbc.update("""
                INSERT INTO desafios_acceso
                (id,id_usuario,proposito,codigo_hash,correo,password_hash,creado,enviado,expira,intentos,envios,usado)
                VALUES (?,?,?,?,?,?,?,?,?,0,1,0)
                """, id,usuario.getIdUsuario(),purpose,encoder.encode(code),usuario.getCorreo(),
                usuario.getPasswordHash(),now,now,now.plusMinutes(10));
        enviar(usuario, code, purpose);
        return pendiente(usuario,id,purpose);
    }

    // Los fallos de código deben conservar el contador incluso si se devuelve HTTP 400.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse confirmar(String id, String codigo) {
        Desafio desafio=buscar(id);
        Usuario usuario=usuarios.bloquearPorId(desafio.usuario()).orElseThrow(this::invalido);
        desafio=buscar(id);
        validar(desafio,usuario);
        if (!encoder.matches(codigo,desafio.hash())) {
            jdbc.update("UPDATE desafios_acceso SET intentos=intentos+1 WHERE id=?",id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto. Máximo cinco intentos por solicitud.");
        }
        jdbc.update("UPDATE desafios_acceso SET usado=1 WHERE id=?",id);
        if ("CORREO".equals(desafio.proposito())) {
            usuario.setCorreoVerificado(true);
            usuarios.save(usuario);
            jdbc.update("UPDATE sesiones_usuario SET revocada=1 WHERE id_usuario=?",usuario.getIdUsuario());
        }
        return new AuthResponse(true,"Acceso verificado",usuario.getIdUsuario(),
                usuario.getPrimerNombre()+" "+usuario.getApellidoPaterno(),sesiones.crearSesion(usuario),null,"COMPLETO");
    }

    @Transactional
    public AuthResponse reenviar(String id) {
        Desafio desafio=buscar(id);
        Usuario usuario=usuarios.bloquearPorId(desafio.usuario()).orElseThrow(this::invalido);
        desafio=buscar(id);
        if (desafio.usado() || desafio.intentos()>=5 || desafio.creado().plusHours(1).isBefore(LocalDateTime.now())
                || !usuario.isActiva() || !usuario.getCorreo().equals(desafio.correo())
                || !usuario.getPasswordHash().equals(desafio.password())) throw invalido();
        if (desafio.envios()>=5 || desafio.enviado().plusSeconds(60).isAfter(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Espera 60 segundos entre envíos. Máximo cinco envíos por solicitud.");
        String code=codigo();
        jdbc.update("UPDATE desafios_acceso SET codigo_hash=?,enviado=?,expira=?,envios=envios+1 WHERE id=?",
                encoder.encode(code),LocalDateTime.now(),LocalDateTime.now().plusMinutes(10),id);
        enviar(usuario,code,desafio.proposito());
        return pendiente(usuario,id,desafio.proposito());
    }

    private void validar(Desafio d,Usuario u) {
        if(d.usado() || d.intentos()>=5 || !d.expira().isAfter(LocalDateTime.now()) ||
                !u.isActiva() || !d.correo().equals(u.getCorreo()) || !d.password().equals(u.getPasswordHash())) throw invalido();
    }
    private Desafio buscar(String id) {
        return jdbc.query("SELECT * FROM desafios_acceso WHERE id=?", (rs,n)->new Desafio(
                rs.getInt("id_usuario"),rs.getString("proposito"),rs.getString("codigo_hash"),
                rs.getString("correo"),rs.getString("password_hash"),rs.getTimestamp("creado").toLocalDateTime(),
                rs.getTimestamp("enviado").toLocalDateTime(),rs.getTimestamp("expira").toLocalDateTime(),
                rs.getInt("intentos"),rs.getInt("envios"),rs.getBoolean("usado")),id).stream().findFirst().orElseThrow(this::invalido);
    }
    private AuthResponse pendiente(Usuario u,String id,String paso) {
        return new AuthResponse(true,"Enviamos un código de seis dígitos a tu correo. Vence en 10 minutos.",
                u.getIdUsuario(),u.getPrimerNombre(),null,id,paso);
    }
    private String codigo(){return String.format(Locale.ROOT,"%06d",random.nextInt(1000000));}
    private void enviar(Usuario usuario,String code,String paso) {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(remitente);message.setTo(usuario.getCorreo());
        message.setSubject("App de mapas · "+("CORREO".equals(paso)?"Verifica tu correo":"Confirma tu inicio de sesión"));
        message.setText("Hola "+usuario.getPrimerNombre()+".\n\nTu código es: "+code+
                "\nVence en 10 minutos. No lo compartas. Si no solicitaste este acceso, ignora el mensaje.");
        try { mail.send(message); }
        catch (org.springframework.mail.MailException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar el código. Intenta nuevamente en unos momentos.");
        }
    }
    private ResponseStatusException invalido(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "La solicitud venció, ya se utilizó o alcanzó el límite de intentos. Inicia sesión nuevamente.");}
    private record Desafio(int usuario,String proposito,String hash,String correo,String password,
                           LocalDateTime creado,LocalDateTime enviado,LocalDateTime expira,int intentos,int envios,boolean usado){}
}
