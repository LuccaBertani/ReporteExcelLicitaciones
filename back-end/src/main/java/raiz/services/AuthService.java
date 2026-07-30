package raiz.services;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import raiz.Repositories.IUsuarioRepository;
import raiz.config.JwtUtil;
import raiz.dominio.Usuario;
import raiz.dtos.output.JwtResponseDtoOutput;
import raiz.dtos.output.MeDtoOutput;

@Service
public class AuthService {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public ResponseEntity<?> iniciarSesion(String username, String password) {
        Usuario usuario = usuarioRepository.findByEmail(username).orElse(null);

        if (usuario == null || !passwordEncoder.matches(password, usuario.getContrasenia())) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Nombre de usuario o contraseña incorrecto");
        }

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> refresh(String authHeaders) {

        if(authHeaders == null || !authHeaders.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Bearer token");
        }

        String token = authHeaders.substring(7);

        Claims claims = JwtUtil.parseClaims(token);
        String email = claims.getSubject();

        Usuario user = this.usuarioRepository.findByEmail(email).orElse(null);

        if(user == null) {
            throw new UsernameNotFoundException(email);
        }

        if(email == null) {
            throw new IllegalArgumentException("Invalid Refresh token");
        }

        String accessToken = JwtUtil.generarAccessToken(email);
        String newRefreshToken = JwtUtil.generarRefreshToken(email);

        return ResponseEntity.ok(new JwtResponseDtoOutput(accessToken, newRefreshToken));
    }

    // Valida el access token actual sin renovarlo (a diferencia de refresh()).
    // Devuelve 401 ante cualquier problema (falta el header, el token no es válido,
    // vencido, o el usuario ya no existe) para que el frontend sepa que la sesión
    // no sirve y dispare su propio flujo de refresh/logout.
    public ResponseEntity<?> me(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = JwtUtil.parseClaims(token);
            String email = claims.getSubject();

            Usuario usuario = this.usuarioRepository.findByEmail(email).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok().body(new MeDtoOutput(email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
