package spring.backend.core.application;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.backend.auth.application.RefreshTokenService;
import spring.backend.auth.application.RotateAccessTokenService;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.auth.infrastructure.redis.repository.RefreshTokenRedisRepository;
import spring.backend.core.exception.DomainException;
import spring.backend.member.domain.entity.Member;
import spring.backend.member.infrastructure.persistence.jpa.adapter.MemberRepositoryImpl;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class RotateAccessTokenServiceTest {
    @Autowired
    private RotateAccessTokenService rotateAccessTokenService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GeoLocationService geoLocationService;

    @Autowired
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private String newIp;
    private String savedIp;


    private final UUID memberId = UUID.randomUUID();

    private final Member member = Member.builder()
            .id(memberId)
            .email("test@test.com")
            .build();
    @Autowired
    private MemberRepositoryImpl memberRepositoryImpl;

    @BeforeEach
    void setUp() {
        newIp = "210.180.165.70"; // 부산
        savedIp = "110.12.115.169"; // 서울
    }

    @AfterEach
    void tearDown() {
        refreshTokenRedisRepository.deleteAll();
    }

    @DisplayName("Cookie에 refreshToken이 존재하지 않는 경우 예외를 발생시킨다.")
    @Test
    void throwExceptionWhenRefreshTokenNotExistsInCookie() {
        // when, then
        assertThatThrownBy(() -> rotateAccessTokenService.rotateToken(null, ""))
                .isInstanceOf(DomainException.class)
                .hasMessage("쿠키값이 존재하지 않습니다.");
    }

    @DisplayName("100km 밖에서 토큰 재발급을 시도한 경우 예외를 발생시킨다.")
    @Test
    void throwExceptionWhenTokenRotateAttemptFrom100km() throws IOException, GeoIp2Exception {
        boolean isOver100km = geoLocationService.checkUserLocation(newIp, savedIp);
        // when, then
        assertThat(isOver100km).isTrue();
    }

    @DisplayName("Redis에 저장된 RefreshToken의 IP와 새로운 IP가 100km 이상 차이가 나는 경우 예외를 발생시킨다.")
    @Test
    void throwExceptionWhenIpDistanceIsOver100km() throws Exception {
        // given
        memberRepositoryImpl.save(member);
        String refreshToken = jwtService.provideRefreshToken(member, savedIp);
        refreshTokenService.saveRefreshToken(refreshToken, member);
        // when, then
        DomainException ex = assertThrows(DomainException.class, () -> rotateAccessTokenService.rotateToken(refreshToken, newIp), "100km 밖에서 토큰 재발급을 시도했습니다.");
        assertThat(ex.getCode()).isEqualTo(AuthenticationErrorCode.TOKEN_ROTATE_ATTEMPT_FROM_INVALID_LOCATION.name());
    }
}
