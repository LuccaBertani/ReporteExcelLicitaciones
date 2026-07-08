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
@CrossOrigin(origins = "*")
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

}
