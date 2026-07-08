package raiz.config;

import lombok.Getter;
import io.jsonwebtoken.*;
import java.util.Date;

public class JwtUtil {
    @Getter
    private static final javax.crypto.SecretKey key = Jwts.SIG.HS256.key().build();
    private static final long ACCESS_TOKEN_VALIDITY = 8 * 60 * 60 * 1000; // 8 horas
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días

    public static String generarAccessToken(String email) {
        return Jwts.builder()
                .subject(email)                                    // setSubject() -> subject()
                .issuer("analisis-licitaciones")                  // setIssuer() -> issuer()
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY)) // setExpiration() -> expiration()
                .signWith(key)                                     // Se mantiene igual (usa la SecretKey moderna)
                .compact();
    }

    public static String generarRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuer("analisis-licitaciones")
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .claim("type", "refresh")                          // Se mantiene igual para claims personalizados
                .signWith(key)
                .compact();
    }

    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) // Cambió setSigningKey() por verifyWith()
                .build()
                .parseSignedClaims(token) // Cambió parseClaimsJws() por parseSignedClaims()
                .getPayload(); // Cambió getBody() por getPayload()
    }
}
