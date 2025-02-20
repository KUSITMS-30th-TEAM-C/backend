package spring.backend.core.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.core.exception.DomainException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String secret;
    private String validJwt;
    private String invalidJwt;
    private String expiredJwt;

    @BeforeEach
    void setUp() {
        long expirationTime = 1;
        Date expiryDate = Date.from(Instant.now().plus(expirationTime, ChronoUnit.DAYS));
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        validJwt = Jwts.builder()
                .claim("name", "test")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        invalidJwt = validJwt + "invalid";

        expiryDate = Date.from(Instant.now().minus(expirationTime, ChronoUnit.DAYS));
        expiredJwt = Jwts.builder()
                .claim("name", "test")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    @DisplayName("유효한 JWT를 파싱하여 claim을 반환한다")
    @Test
    void getPayloadWithValidJwt() {
        // when
        Claims claims = jwtService.getPayload(validJwt);

        // then
        assertThat(claims.get("name")).isEqualTo("test");
    }

    @DisplayName("만료된 토큰일 경우 예외를 발생시킨다")
    @Test
    void validateTokenExpirationWithExpiredJwt() {
        // when & then
        DomainException ex = assertThrows(DomainException.class, () -> jwtService.validateTokenExpiration(expiredJwt), "만료된 토큰입니다.");
        assertThat(ex.getCode()).isEqualTo(AuthenticationErrorCode.EXPIRED_TOKEN.name());
    }
}
