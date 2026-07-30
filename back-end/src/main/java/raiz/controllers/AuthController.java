package raiz.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raiz.config.JwtUtil;
import raiz.dtos.input.LoginDtoInput;
import raiz.dtos.output.JwtResponseDtoOutput;
import raiz.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDtoInput loginDtoInput) {

        String email = loginDtoInput.getEmail();
        String password = loginDtoInput.getPassword();

        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ResponseEntity<?> rta = this.authService.iniciarSesion(email, password);

        if (!rta.getStatusCode().is2xxSuccessful()){
            return rta;
        }

        String accessToken = JwtUtil.generarAccessToken(email);
        String refreshToken = JwtUtil.generarRefreshToken(email);

        JwtResponseDtoOutput response = JwtResponseDtoOutput.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {
        return this.authService.refresh(authHeader);
    }

    // Endpoint liviano para validar si el access token actual sigue siendo válido.
    // Lo usa el frontend una sola vez al montar la app (AuthContext), para no confiar
    // solo en "hay un token en localStorage" y así evitar que Home/Upload naveguen
    // con una sesión vencida hasta que el Dashboard la detecta recién al pedir datos.
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) final String authHeader) {
        return this.authService.me(authHeader);
    }

}
