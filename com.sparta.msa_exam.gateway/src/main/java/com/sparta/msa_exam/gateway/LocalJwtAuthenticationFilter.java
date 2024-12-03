package com.sparta.msa_exam.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LocalJwtAuthenticationFilter implements GlobalFilter {

    @Value("${service.jwt.secret-key}")
    private String secretKey;

    @Value("${service.jwt.issuer}")
    private String expectedIssuer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 인증이 필요 없는 경로 예외 처리
        if (path.equals("/auth/sign-in") || path.equals("/auth/sign-up")) {
            return chain.filter(exchange);
        }

        // 특정 경로만 로그인한 사용자 접근 허용
        String token = extractToken(exchange);

        // 토큰이 없거나 유효하지 않으면 요청 차단
        if (token == null || !validateToken(token, exchange)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token, ServerWebExchange exchange) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));

            // JWT 검증 및 클레임 추출
            Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

            Claims claims = claimsJws.getBody();

            // OAuth2 규칙 검증
            if (!expectedIssuer.equals(claims.getIssuer())) {
                log.warn("Issuer mismatch. Expected: " + expectedIssuer + ", Found: "
                    + claims.getIssuer());
                return false;
            }

            if (isTokenExpired(claims.getExpiration())) {
                log.warn("Token has expired.");
                return false;
            }

            exchange.getRequest().mutate()
                .header("X-User-Id", claims.get("user_id").toString())
                .header("X-Role", claims.get("role").toString())
                .build();
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed", e);
            return false;
        }
    }

    private boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }
}
