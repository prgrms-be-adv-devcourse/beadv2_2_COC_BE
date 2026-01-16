package com.coc.modi.member.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Getter
@Component
public class JwtTokenProvider {
	
    private final Key signingKey;
	private final long accessTokenValidityInMs;
    private final long refreshTokenValidityInMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
							@Value("${jwt.access-token-expiration}") long accessTokenValidityInMs,
							@Value("${jwt.refresh-token-expiration}") long refreshTokenValidityInMs) {
		
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }
	
	public String generateAccessToken(Long memberId, String name, String email){

        return generateToken(memberId, name, email, accessTokenValidityInMs);
    }

    public String generateRefreshToken(Long memberId, String name, String email){

        return generateToken(memberId, name, email, refreshTokenValidityInMs);
    }


    public String generateToken(Long memberId,
								String name,
								String email,
                                long validityInMs){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(memberId.toString())
				.claim("name", name)
				.claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
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

    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
	
	public String getName(String refreshToken) {
		
		return getClaims(refreshToken).get("name").toString();
	}
	
	public String getEmail(String refreshToken) {
		
		return getClaims(refreshToken).get("email").toString();
	}
}
