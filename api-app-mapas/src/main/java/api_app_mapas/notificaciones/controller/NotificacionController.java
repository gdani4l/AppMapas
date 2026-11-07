package api_app_mapas.notificaciones.controller;

import api_app_mapas.notificaciones.dto.NotificacionResponse;
import api_app_mapas.notificaciones.repository.NotificacionRepository;
import api_app_mapas.sesion.service.SesionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionRepository repository;
    private final SesionService sesiones;
    public NotificacionController(NotificacionRepository repository, SesionService sesiones) {
        this.repository = repository; this.sesiones = sesiones;
    }
    @GetMapping
    public List<NotificacionResponse> listar(@RequestHeader("Authorization") String auth) {
        Integer id = sesiones.obtenerUsuario(auth).getIdUsuario();
        return repository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(id).stream()
                .map(NotificacionResponse::desde).toList();
    }
    @PutMapping("/{id}/leida")
    public NotificacionResponse leer(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Integer userId = sesiones.obtenerUsuario(auth).getIdUsuario();
        var value = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));
        if (!value.getUsuario().getIdUsuario().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar esta notificación");
        value.setLeida(true);
        return NotificacionResponse.desde(repository.save(value));
    }
}
