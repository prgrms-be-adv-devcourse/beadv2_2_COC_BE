package com.coc.modi.auth.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final long accessTokenValidityInMs;
    private final long refreshTokenValidityInMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-expiration}") long accessTokenValidityInMs,
                            @Value("${jwt.refresh-token-expiration}") long refreshTokenValidityInMs) {

        this.secretKey = secretKey;
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    public String generateAccessToken(Long memberId, String role){

        return generateToken(memberId, role, accessTokenValidityInMs);
    }

    public String generateRefreshToken(Long memberId, String role){

        return generateToken(memberId, role, refreshTokenValidityInMs);
    }


    public String generateToken(Long memberId,
                                String role,
                                long validityInMs){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token){

        try {
            getClaims(token);

            return true;
        } catch (Exception e) {

            return false;
        }
    }

    public Long getMemberId(String token){

        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getRole(String token){

        return getClaims(token).get("role").toString();
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

}
