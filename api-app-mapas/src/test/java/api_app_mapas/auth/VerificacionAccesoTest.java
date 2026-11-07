package api_app_mapas.auth;

import api_app_mapas.auth.dto.AuthResponse;
import api_app_mapas.auth.service.VerificacionAccesoService;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificacionAccesoTest {
    JdbcTemplate jdbc;
    Usuario usuario;
    SesionService sesiones;
    JavaMailSender mail;
    VerificacionAccesoService service;

    @BeforeEach void setup() {
        var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa","");
        jdbc=new JdbcTemplate(ds);
        jdbc.execute("""
            CREATE TABLE desafios_acceso (
             id varchar(36) PRIMARY KEY, id_usuario int, proposito varchar(20), codigo_hash varchar(100),
             correo varchar(254), password_hash varchar(255), creado timestamp, enviado timestamp,
             expira timestamp, intentos int, envios int, usado boolean)
            """);
        jdbc.execute("CREATE TABLE sesiones_usuario (id_usuario int, revocada boolean)");
        var usuarios=mock(UsuarioRepository.class);sesiones=mock(SesionService.class);mail=mock(JavaMailSender.class);
        usuario=new Usuario();usuario.setIdUsuario(1);usuario.setPrimerNombre("Prueba");
        usuario.setApellidoPaterno("Local");usuario.setCorreo("prueba@example.invalid");
        usuario.setPasswordHash("hash-original");usuario.setActiva(true);
        when(usuarios.bloquearPorId(1)).thenReturn(Optional.of(usuario));
        when(sesiones.crearSesion(usuario)).thenReturn("sesion-de-prueba");
        var target=new VerificacionAccesoService(jdbc,usuarios,sesiones,new BCryptPasswordEncoder(4),mail,"no-reply@example.invalid");
        ProxyFactory proxy=new ProxyFactory(target);proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(ds),new AnnotationTransactionAttributeSource()));
        service=(VerificacionAccesoService)proxy.getProxy();
    }
    private String lastCode() {
        ArgumentCaptor<SimpleMailMessage> cap=ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail,atLeastOnce()).send(cap.capture());
        var matcher=java.util.regex.Pattern.compile("código es: ([0-9]{6})").matcher(cap.getValue().getText());
        assertTrue(matcher.find());return matcher.group(1);
    }
    @Test void registrationRequiresCodeAndRevokesOldSessions() {
        jdbc.update("INSERT INTO sesiones_usuario VALUES (1,false)");
        AuthResponse pending=service.iniciar(usuario);
        assertNull(pending.tokenSesion());assertEquals("CORREO",pending.paso());
        verify(sesiones,never()).crearSesion(any());
        AuthResponse completed=service.confirmar(pending.desafioId(),lastCode());
        assertTrue(usuario.isCorreoVerificado());assertNotNull(completed.tokenSesion());
        assertTrue(jdbc.queryForObject("SELECT revocada FROM sesiones_usuario",Boolean.class));
        assertThrows(ResponseStatusException.class,()->service.confirmar(pending.desafioId(),lastCode()));
        verify(sesiones,times(1)).crearSesion(usuario);
    }
    @Test void verifiedUserStillNeedsSecondStep() {
        usuario.setCorreoVerificado(true);
        var pending=service.iniciar(usuario);
        assertEquals("LOGIN",pending.paso());assertNull(pending.tokenSesion());
        assertNotNull(service.confirmar(pending.desafioId(),lastCode()).tokenSesion());
    }
    @Test void wrongAttemptsPersistAndLockAfterFive() {
        var pending=service.iniciar(usuario);String correct=lastCode();
        String wrong=correct.equals("000000")?"111111":"000000";
        for(int i=0;i<5;i++)assertThrows(ResponseStatusException.class,()->service.confirmar(pending.desafioId(),wrong));
        assertEquals(5,jdbc.queryForObject("SELECT intentos FROM desafios_acceso",Integer.class));
        assertThrows(ResponseStatusException.class,()->service.confirmar(pending.desafioId(),correct));
        verify(sesiones,never()).crearSesion(any());
    }
    @Test void expiredCodeCannotCreateSession() {
        var pending=service.iniciar(usuario);String code=lastCode();
        jdbc.update("UPDATE desafios_acceso SET expira=?",java.time.LocalDateTime.now().minusSeconds(1));
        assertThrows(ResponseStatusException.class,()->service.confirmar(pending.desafioId(),code));
        verify(sesiones,never()).crearSesion(any());
    }
    @Test void suspendedAccountCannotStartOrCompleteChallenge() {
        var pending=service.iniciar(usuario);String code=lastCode();usuario.setActiva(false);
        assertThrows(ResponseStatusException.class,()->service.confirmar(pending.desafioId(),code));
        assertThrows(ResponseStatusException.class,()->service.iniciar(usuario));
    }
    @Test void passwordChangeInvalidatesChallenge() {
        var pending=service.iniciar(usuario);String code=lastCode();usuario.setPasswordHash("new-hash");
        assertThrows(ResponseStatusException.class,()->service.confirmar(pending.desafioId(),code));
    }
    @Test void resendCooldownAndAttemptBudgetAreEnforced() {
        var pending=service.iniciar(usuario);
        assertEquals(429,assertThrows(ResponseStatusException.class,()->service.reenviar(pending.desafioId())).getStatusCode().value());
        jdbc.update("UPDATE desafios_acceso SET enviado=?,intentos=3",java.time.LocalDateTime.now().minusMinutes(2));
        service.reenviar(pending.desafioId());
        assertEquals(3,jdbc.queryForObject("SELECT intentos FROM desafios_acceso",Integer.class));
        assertEquals(2,jdbc.queryForObject("SELECT envios FROM desafios_acceso",Integer.class));
    }
    @Test void emailDeliveryFailureRollsBackChallenge() {
        doThrow(new org.springframework.mail.MailSendException("simulated")).when(mail).send(any(SimpleMailMessage.class));
        assertThrows(ResponseStatusException.class,()->service.iniciar(usuario));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM desafios_acceso",Integer.class));
    }
}
