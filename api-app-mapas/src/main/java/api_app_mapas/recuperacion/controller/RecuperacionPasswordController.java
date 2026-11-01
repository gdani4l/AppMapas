package api_app_mapas.recuperacion.controller;

import api_app_mapas.recuperacion.dto.MensajeResponse;
import api_app_mapas.recuperacion.dto.RestablecerPasswordRequest;
import api_app_mapas.recuperacion.dto.SolicitudRecuperacionRequest;
import api_app_mapas.recuperacion.service.RecuperacionPasswordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/recuperacion")
public class RecuperacionPasswordController {

    private final RecuperacionPasswordService recuperacionPasswordService;

    public RecuperacionPasswordController(RecuperacionPasswordService recuperacionPasswordService) {
        this.recuperacionPasswordService = recuperacionPasswordService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<MensajeResponse> solicitarCodigo(
            @Valid @RequestBody SolicitudRecuperacionRequest request
    ) {
        return ResponseEntity.ok(recuperacionPasswordService.solicitarCodigo(request));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecerPassword(
            @Valid @RequestBody RestablecerPasswordRequest request
    ) {
        return ResponseEntity.ok(recuperacionPasswordService.restablecerPassword(request));
    }
}
