package spring.backend.auth.application;

import io.lettuce.core.RedisConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.backend.auth.domain.repository.RefreshTokenRepository;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.core.application.JwtService;
import spring.backend.core.exception.DomainException;
import spring.backend.core.exception.error.GlobalErrorCode;
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
        try {
            refreshTokenRepository.save(member.getId(), jwtService.provideRefreshToken(member),REFRESH_TOKEN_EXPIRATION, convertChronoUnitToTimeUnit(ChronoUnit.DAYS));
            return getRefreshToken(member.getId());
        } catch (RedisConnectionException e) {
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }

    public String getRefreshToken(UUID memberId) {
        try {
            String refreshToken =  refreshTokenRepository.findByMemberId(memberId);
            if (refreshToken == null || refreshToken.isEmpty()) {
                log.error("리프레시 토큰이 저장소에 존재하지 않습니다.");
                throw AuthenticationErrorCode.NOT_EXIST_REFRESH_TOKEN.toException();
            }
            return refreshToken;
        } catch (RedisConnectionException e) {
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }

    public void deleteRefreshToken(UUID memberId) {
        try {
            refreshTokenRepository.delete(memberId);
        } catch (RedisConnectionException e) {
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
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
