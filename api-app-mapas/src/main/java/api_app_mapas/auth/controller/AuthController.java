package api_app_mapas.auth.controller;

import api_app_mapas.auth.dto.AuthResponse;
import api_app_mapas.auth.dto.LoginRequest;
import api_app_mapas.auth.dto.RegistroRequest;
import api_app_mapas.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final api_app_mapas.auth.service.VerificacionAccesoService verificacion;

    public AuthController(AuthService authService, api_app_mapas.auth.service.VerificacionAccesoService verificacion) {
        this.authService = authService;
        this.verificacion = verificacion;
    }

    public record ConfirmarRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=36) String desafioId,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Pattern(regexp="\\d{6}") String codigo) { }
    public record ReenviarRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=36) String desafioId) { }

    @PostMapping("/verificacion/confirmar")
    public AuthResponse confirmar(@Valid @RequestBody ConfirmarRequest request) {
        return verificacion.confirmar(request.desafioId(),request.codigo());
    }
    @PostMapping("/verificacion/reenviar")
    public AuthResponse reenviar(@Valid @RequestBody ReenviarRequest request) {
        return verificacion.reenviar(request.desafioId());
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }
}
