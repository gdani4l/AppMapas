package api_app_mapas.administracion;

import api_app_mapas.administracion.controller.CuentasController;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CuentasControllerTest {
    JdbcTemplate jdbc;Usuario actor;CuentasController controller;
    @BeforeEach void setup(){
        jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
        jdbc.execute("CREATE TABLE usuarios (id_usuario int PRIMARY KEY, primer_nombre varchar(50), segundo_nombre varchar(50), apellido_paterno varchar(50), apellido_materno varchar(50), correo varchar(254), rol varchar(20), activa tinyint, correo_verificado tinyint, fecha_registro timestamp DEFAULT CURRENT_TIMESTAMP)");
        jdbc.execute("CREATE TABLE sesiones_usuario (id_usuario int, revocada tinyint)");
        jdbc.execute("CREATE TABLE desafios_acceso (id_usuario int, usado tinyint)");
        jdbc.execute("CREATE TABLE auditoria_cuentas (id_admin int, id_usuario int, accion varchar(100))");
        jdbc.update("INSERT INTO usuarios (id_usuario,primer_nombre,apellido_paterno,correo,rol,activa,correo_verificado) VALUES (1,'Admin','Prueba','admin@example.invalid','ADMIN',1,1),(2,'Persona','Prueba','persona@example.invalid','USUARIO',1,0)");
        jdbc.update("INSERT INTO sesiones_usuario VALUES (2,0)");jdbc.update("INSERT INTO desafios_acceso VALUES (2,0)");
        actor=new Usuario();actor.setIdUsuario(1);actor.setRol("ADMIN");
        var sessions=mock(SesionService.class);when(sessions.obtenerUsuario("Bearer test")).thenReturn(actor);
        controller=new CuentasController(jdbc,sessions);
    }
    @Test void nonAdminCannotListOrManage(){
        actor.setRol("USUARIO");
        assertEquals(403,assertThrows(ResponseStatusException.class,()->controller.listar("Bearer test","","TODOS","TODOS","TODOS",0)).getStatusCode().value());
        assertThrows(ResponseStatusException.class,()->controller.modificar("Bearer test",2,new CuentasController.Accion("SUSPENDER")));
    }
    @Test void filtersAndSearchReturnOnlyMatchingAccounts(){
        var result=controller.listar("Bearer test","persona","ACTIVA","USUARIO","NO",0);
        assertEquals(1L,result.get("total"));
        assertFalse(result.toString().contains("password"));
        assertEquals(0L,controller.listar("Bearer test","missing","TODOS","TODOS","TODOS",0).get("total"));
    }
    @Test void suspendRevokesSessionsAndCodesAndRecordsAudit(){
        controller.modificar("Bearer test",2,new CuentasController.Accion("SUSPENDER"));
        assertEquals(0,jdbc.queryForObject("SELECT activa FROM usuarios WHERE id_usuario=2",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT revocada FROM sesiones_usuario",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT usado FROM desafios_acceso",Integer.class));
        assertEquals("SUSPENDER",jdbc.queryForObject("SELECT accion FROM auditoria_cuentas",String.class));
    }
    @Test void selfModificationIsRejected(){
        assertThrows(ResponseStatusException.class,()->controller.modificar("Bearer test",1,new CuentasController.Accion("USUARIO")));
        assertEquals("ADMIN",jdbc.queryForObject("SELECT rol FROM usuarios WHERE id_usuario=1",String.class));
    }
}
