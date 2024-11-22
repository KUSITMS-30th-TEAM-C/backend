package spring.backend.core.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
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

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.access-token-expiry}") long accessTokenExpiry, @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ACCESS_EXPIRATION = accessTokenExpiry;
        this.REFRESH_EXPIRATION = refreshTokenExpiry;
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
        return provideToken(
                member.getEmail(),
                member.getId(),
                Type.REFRESH,
                REFRESH_EXPIRATION
        );
    }

    public Claims getPayload(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            log.error("Invalid signature", e);
            throw AuthenticationErrorCode.INVALID_SIGNATURE.toException();
        } catch (ExpiredJwtException e) {
            log.error("Expired token", e);
            throw AuthenticationErrorCode.EXPIRED_TOKEN.toException();
        } catch (MalformedJwtException e) {
            log.error("Invalid token format", e);
            throw AuthenticationErrorCode.NOT_MATCH_TOKEN_FORMAT.toException();
        } catch (Exception e) {
            log.error("Failed to parse token", e);
            throw AuthenticationErrorCode.NOT_DEFINE_TOKEN.toException();
        }
    }

    public UUID extractMemberId(String token) {
        Claims claims = getPayload(token);
        return UUID.fromString(claims.get("memberId", String.class));
    }

    public void validateTokenExpiration(String token) {
        Claims claims = getPayload(token);
        if (claims.getExpiration().before(new Date())) {
            log.error("Token has expired, token: {}", token);
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

    public UUID extractMemberIdFromExpiredAccessToken(String invalidAccessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(invalidAccessToken)
                    .getPayload();
            return UUID.fromString(claims.get("memberId", String.class));
        } catch (ExpiredJwtException e) {
            return UUID.fromString(e.getClaims().get("memberId", String.class));
        } catch (Exception e) {
            log.error("Failed to extract userId from invalid token", e);
            throw AuthenticationErrorCode.FAILED_TO_EXTRACT_MEMBER_ID_FROM_EXPIRED_ACCESS_TOKEN.toException();
        }
    }
}
