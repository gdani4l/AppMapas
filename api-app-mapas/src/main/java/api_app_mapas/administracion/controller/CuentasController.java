package api_app_mapas.administracion.controller;

import api_app_mapas.sesion.service.SesionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/admin/cuentas")
public class CuentasController {
    private final JdbcTemplate jdbc;
    private final SesionService sesiones;
    public CuentasController(JdbcTemplate jdbc,SesionService sesiones){this.jdbc=jdbc;this.sesiones=sesiones;}

    @GetMapping
    public Map<String,Object> listar(@RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue="") String buscar,
            @RequestParam(defaultValue="TODOS") String estado,
            @RequestParam(defaultValue="TODOS") String rol,
            @RequestParam(defaultValue="TODOS") String verificado,
            @RequestParam(defaultValue="0") int pagina) {
        admin(auth);
        if(pagina<0 || pagina>100000 || buscar.length()>100
                || !Set.of("TODOS","ACTIVA","SUSPENDIDA").contains(estado)
                || !Set.of("TODOS","ADMIN","USUARIO").contains(rol)
                || !Set.of("TODOS","SI","NO").contains(verificado)) throw error("Filtros inválidos");
        List<Object> args=new ArrayList<>();
        StringBuilder where=new StringBuilder(" WHERE 1=1");
        if(!buscar.isBlank()) {
            where.append(" AND (CONCAT_WS(' ',primer_nombre,segundo_nombre,apellido_paterno,apellido_materno) LIKE ? OR correo LIKE ?)");
            String term="%"+buscar.trim()+"%";args.add(term);args.add(term);
        }
        if(!"TODOS".equals(estado)){where.append(" AND activa=?");args.add("ACTIVA".equals(estado));}
        if(!"TODOS".equals(rol)){where.append(" AND rol=?");args.add(rol);}
        if(!"TODOS".equals(verificado)){where.append(" AND correo_verificado=?");args.add("SI".equals(verificado));}
        Long total=jdbc.queryForObject("SELECT COUNT(*) FROM usuarios"+where,Long.class,args.toArray());
        args.add(25);args.add(pagina*25);
        var rows=jdbc.queryForList("SELECT id_usuario, CONCAT_WS(' ',primer_nombre,apellido_paterno,apellido_materno) nombre, correo, rol, activa, correo_verificado, fecha_registro FROM usuarios"+where+" ORDER BY id_usuario DESC LIMIT ? OFFSET ?",args.toArray());
        return Map.of("cuentas",rows,"total",total,"pagina",pagina,"tamanoPagina",25);
    }

    public record Accion(@NotBlank @Pattern(regexp="SUSPENDER|ACTIVAR|ADMIN|USUARIO|CERRAR_SESIONES") String accion){}
    @PutMapping("/{id}")
    @Transactional
    public Map<String,String> modificar(@RequestHeader("Authorization") String auth,
                                       @PathVariable int id,@Valid @RequestBody Accion request) {
        int actor=admin(auth);
        // Orden único de bloqueos: serializa cambios de administradores concurrentes.
        jdbc.queryForList("SELECT id_usuario FROM usuarios WHERE rol='ADMIN' ORDER BY id_usuario FOR UPDATE");
        Long autorizado=jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE id_usuario=? AND rol='ADMIN' AND activa=1 AND correo_verificado=1",Long.class,actor);
        if(autorizado==0)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Tu cuenta ya no puede administrar usuarios");
        if(actor==id)throw error("No puedes modificar tu propia cuenta desde este panel");
        var rows=jdbc.queryForList("SELECT rol,activa,correo_verificado FROM usuarios WHERE id_usuario=? FOR UPDATE",id);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Cuenta no encontrada");
        String accion=request.accion();
        if(("SUSPENDER".equals(accion)||"USUARIO".equals(accion)) && "ADMIN".equals(rows.get(0).get("rol"))) {
            Long count=jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE rol='ADMIN' AND activa=1 AND correo_verificado=1",Long.class);
            if(count<=1)throw error("Debe existir al menos un administrador activo con correo verificado");
        }
        switch(accion) {
            case "ACTIVAR" -> jdbc.update("UPDATE usuarios SET activa=1 WHERE id_usuario=?",id);
            case "SUSPENDER" -> jdbc.update("UPDATE usuarios SET activa=0 WHERE id_usuario=?",id);
            case "ADMIN","USUARIO" -> jdbc.update("UPDATE usuarios SET rol=? WHERE id_usuario=?",accion,id);
            case "CERRAR_SESIONES" -> { }
            default -> throw error("Acción inválida");
        }
        jdbc.update("UPDATE sesiones_usuario SET revocada=1 WHERE id_usuario=?",id);
        jdbc.update("UPDATE desafios_acceso SET usado=1 WHERE id_usuario=?",id);
        jdbc.update("INSERT INTO auditoria_cuentas (id_admin,id_usuario,accion) VALUES (?,?,?)",actor,id,accion);
        return Map.of("mensaje","Cuenta actualizada. Sus sesiones anteriores se cerraron.");
    }

    private int admin(String auth){
        var user=sesiones.obtenerUsuario(auth);
        if(!"ADMIN".equalsIgnoreCase(user.getRol()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Se requiere una cuenta administradora");
        return user.getIdUsuario();
    }
    private ResponseStatusException error(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
}
