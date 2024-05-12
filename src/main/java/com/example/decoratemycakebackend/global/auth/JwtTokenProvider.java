package com.example.decoratemycakebackend.global.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private final long accessTokenValidityInMilliseconds = 60 * 60 * 1000L; // 1시간
    private final long refreshTokenValidityInMilliseconds = 14 * 24 * 60 * 60 * 1000L; // 2주

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final CustomUserDetailsService customUserDetailsService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Member 정보 가지고 AccessToken, RefreshToken 생성
    public JwtToken generateToken(Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpireIn = new Date(now + accessTokenValidityInMilliseconds);
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(accessTokenExpireIn)
                .signWith(key)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(new Date(now + refreshTokenValidityInMilliseconds))
                .signWith(key)
                .compact();

        // Refresh Token을 Redis에 저장
        redisTemplate.opsForValue().set(authentication.getName(), refreshToken, refreshTokenValidityInMilliseconds, TimeUnit.MILLISECONDS);

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼냄
    public Authentication getAuthentication(String accessToken) {
        // Jwt 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // claims에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        // UserDetails 객체를 만들어서 Authentication return
        // UserDetails는 interface이고, User는 그것을 구현한 class
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
        }
        return false;
    }

    public JwtToken refreshAccessToken(String refreshToken) {
        // 요청받은 Refresh Token의 유효성 검사
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        // Refresh Token에서 사용자 정보 추출
        Claims claims = parseClaims(refreshToken);
        String username = claims.getSubject();

        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Invalid Refresh Token: Missing user information");
        }

        // Redis에 저장된 Refresh Token 가져오기
        String savedRefreshToken = (String)redisTemplate.opsForValue().get(username);

        if (savedRefreshToken == null) {
            throw new RuntimeException("Refresh Token not found in Redis");
        }

        // 요청받은 Refresh Token과 Redis에 저장된 Refresh Token 비교
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new RuntimeException("Refresh Token is not matched from redis");
        }

        // Refresh Token의 만료 시간 확인
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            // Refresh Token이 만료된 경우 Redis에서 해당 토큰 삭제. 사용자는 새로 로그인 해야함.
            redisTemplate.delete(username);
            throw new RuntimeException("Refresh Token has expired");
        }

        // 새로운 Access Token 생성
        long now = (new Date()).getTime();
        Date accessTokenExpireIn = new Date(now + accessTokenValidityInMilliseconds);
        String newAccessToken = Jwts.builder()
                .setSubject(username)
                .claim("auth", getUserAuthorities(username))
                .setExpiration(accessTokenExpireIn)
                .signWith(key)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(savedRefreshToken)
                .build();
    }

    private String getUserAuthorities(String username) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Redis에 저장된 RefreshToken을 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username);
    }
}