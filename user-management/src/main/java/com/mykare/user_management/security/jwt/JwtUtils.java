package com.mykare.user_management.security.jwt;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mykare.user_management.model.User;
import com.mykare.user_management.security.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${annular.app.jwtSecret}")
    private String jwtSecret;

    @Value("${annular.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Claims claims = Jwts.claims();
        claims.put("userEmailId", userPrincipal.getUserEmailId());

        byte[] keyBytes = new byte[64];
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");

        return Jwts.builder().setSubject(userPrincipal.getUsername()).setClaims(claims).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, key).compact();
    }

    public String generateJwtTokenForRefreshToken(User user) {

//		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Claims claims = Jwts.claims();
        claims.put("userEmailId", user.getEmailId());


        byte[] keyBytes = new byte[64];
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");

        return Jwts.builder().setSubject(user.getEmailId()).setClaims(claims).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, key).compact();
    }

    public String getUserNameFromJwtToken(String token) {
        byte[] keyBytes = new byte[64];
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
    }

    public String getDataFromJwtToken(String token, String key) {
        byte[] keyBytes = new byte[64];
        SecretKey key1 = new SecretKeySpec(keyBytes, "HmacSHA512");
        return Jwts.parser().setSigningKey(key1).parseClaimsJws(token).getBody().get(key).toString();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            byte[] keyBytes = new byte[64];
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");
            Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}