package spring.backend.auth.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.backend.auth.domain.repository.RefreshTokenRepository;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.core.application.JwtService;
import spring.backend.member.domain.entity.Member;

import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RefreshTokenService {
    private final JwtService jwtService;
    private final long REFRESH_TOKEN_EXPIRATION;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtService jwtService, @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.REFRESH_TOKEN_EXPIRATION = refreshTokenExpiry;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String saveRefreshToken(Member member) {
        refreshTokenRepository.save(member.getId(), jwtService.provideRefreshToken(member), REFRESH_TOKEN_EXPIRATION, convertChronoUnitToTimeUnit(ChronoUnit.DAYS));
        return getRefreshToken(member.getId());
    }

    public String getRefreshToken(UUID memberId) {
        String refreshToken = refreshTokenRepository.findByMemberId(memberId);

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.error("리프레시 토큰이 저장소에 존재하지 않습니다.");
            throw AuthenticationErrorCode.NOT_EXIST_REFRESH_TOKEN.toException();
        }

        jwtService.getPayload(refreshToken);
        return refreshToken;
    }

    public void deleteRefreshToken(UUID memberId) {

        if (refreshTokenRepository.findByMemberId(memberId) == null) {
            log.error("memberId에 해당하는 리프레시 토큰이 저장소에 존재하지 않습니다.");
            throw AuthenticationErrorCode.NOT_EXIST_REFRESH_TOKEN.toException();
        }

        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private TimeUnit convertChronoUnitToTimeUnit(ChronoUnit chronoUnit) {
        switch (chronoUnit) {
            case NANOS:
                return TimeUnit.NANOSECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case HOURS:
                return TimeUnit.HOURS;
            case DAYS:
                return TimeUnit.DAYS;
            default:
                throw AuthenticationErrorCode.UNSUPPORTED_REDIS_TIME_TYPE.toException();
        }
    }
}
