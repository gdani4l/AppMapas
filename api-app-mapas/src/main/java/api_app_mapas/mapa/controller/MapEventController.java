package api_app_mapas.mapa.controller;

import api_app_mapas.mapa.service.MapEventService;
import api_app_mapas.sesion.service.SesionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/mapa")
public class MapEventController {
    private final MapEventService events;
    private final SesionService sessions;

    public MapEventController(MapEventService events, SesionService sessions) {
        this.events = events;
        this.sessions = sessions;
    }

    @GetMapping(value = "/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@RequestHeader("Authorization") String authorization) {
        sessions.obtenerUsuario(authorization);
        return events.subscribe();
    }
}
