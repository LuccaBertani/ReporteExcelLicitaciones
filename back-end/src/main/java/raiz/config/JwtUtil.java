package raiz.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    @Getter
    private static final SecretKey key = resolveKey();

    private static final long ACCESS_TOKEN_VALIDITY = 8 * 60 * 60 * 1000; // 8 horas
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días

    // Lee la clave desde la variable de entorno JWT_SECRET (Base64, 256 bits para HS256).
    // Si no está definida (por ej. corriendo desde el IDE sin configurar el .env),
    // genera una al azar SOLO como fallback de desarrollo: avisa por consola porque
    // en ese caso las sesiones no sobreviven a un reinicio.
    private static SecretKey resolveKey() {
        String secret = System.getenv("JWT_SECRET");
        System.out.println("[DEBUG] JWT_SECRET leída del entorno: " + secret);
        if (secret == null || secret.isBlank()) {
            System.out.println("[AVISO] JWT_SECRET no está definida: se genera una clave temporal en memoria. " +
                    "Las sesiones no van a sobrevivir a un reinicio del backend. Definila en tu .env para un entorno estable.");
            return Jwts.SIG.HS256.key().build();
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public static String generarAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuer("analisis-licitaciones")
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(key)
                .compact();
    }

    public static String generarRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuer("analisis-licitaciones")
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
