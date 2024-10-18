package spring.backend.core.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.member.domain.entity.Member;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


@Service
public class JwtService {

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        ACCESS("access"), REFRESH("refresh");
        private final String type;
    }

    private final SecretKey SECRET_KEY;
    private final long ACCESS_EXPIRATION;
    private final long REFRESH_EXPIRATION;
    private final RedisService redisService;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.access-token-expiry}") long accessTokenExpiry, @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry, RedisService redisService) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ACCESS_EXPIRATION = accessTokenExpiry;
        this.REFRESH_EXPIRATION = refreshTokenExpiry;
        this.redisService = redisService;
    }

    public String provideAccessToken(Member member) {
        return provideToken(
                member.getEmail(),
                member.getId(),
                Type.ACCESS,
                ACCESS_EXPIRATION
        );
    }

    public String provideRefreshToken(Member member) {
        String refreshToken = provideToken(
                member.getEmail(),
                member.getId(),
                Type.REFRESH,
                REFRESH_EXPIRATION
        );

        redisService.saveRefreshToken(member.getId(), refreshToken, REFRESH_EXPIRATION, ChronoUnit.DAYS);

        return refreshToken;
    }

    public Claims getPayload(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            throw AuthenticationErrorCode.INVALID_SIGNATURE.toException();
        } catch (ExpiredJwtException e) {
            throw AuthenticationErrorCode.EXPIRED_TOKEN.toException();
        } catch (MalformedJwtException e) {
            throw AuthenticationErrorCode.NOT_MATCH_TOKEN_FORMAT.toException();
        } catch (Exception e) {
            throw AuthenticationErrorCode.NOT_DEFINE_TOKEN.toException();
        }
    }

    public void validateTokenExpiration(String token) {
        Claims claims = getPayload(token);
        if (claims.getExpiration().before(new Date())) {
            throw AuthenticationErrorCode.EXPIRED_TOKEN.toException();
        }
    }

    private String provideToken(String email, UUID id, Type type, long expiration) {
        Date expiryDate = Date.from(Instant.now().plus(expiration, ChronoUnit.DAYS));
        return Jwts.builder()
                .claims(Map.of(
                        "memberId", id.toString(),
                        "email", email,
                        "type", type.getType()))
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }
}
