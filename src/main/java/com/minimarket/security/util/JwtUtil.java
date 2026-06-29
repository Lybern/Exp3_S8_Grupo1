package com.minimarket.security.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.security.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final JwtProperties props;
    private final Key key;

    @Autowired
    public JwtUtil(JwtProperties props) {
        this.props = props;
        byte[] keyBytes = props.getSecret() != null
                ? props.getSecret().getBytes(StandardCharsets.UTF_8)
                : new byte[0];
        // Ensure key length >= 32 bytes for HS256
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("La clave secreta JWT es demasiado corta. Debe tener al menos 32 bytes (256 bits).");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

   public String generateToken(Usuario usuario) {

    long now = System.currentTimeMillis();

    return Jwts.builder()
            .setSubject(usuario.getUsername())

            .claim("roles",
                    usuario.getRoles()
                            .stream()
                            .map(Rol::getNombre)
                            .toList())

            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + props.getExpiration()))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
}

    public String extractUsername(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            throw e;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        // Aseguramos que username no sea nulo antes de ejecutar .equals()
        return (username != null && username.equals(tokenUsername) && !isTokenExpired(token));
    }

}