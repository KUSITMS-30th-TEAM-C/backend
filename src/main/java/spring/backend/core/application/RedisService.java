package spring.backend.core.application;

import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.backend.core.exception.error.GlobalErrorCode;
import spring.backend.core.infrastructure.redis.repository.RedisRepository;

import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisRepository redisRepository;

    public void saveRefreshToken(UUID memberId, String refreshToken, Long expireTime, ChronoUnit chronoUnit) {
        try {
            TimeUnit timeUnit = convertChronoUnitToTimeUnit(chronoUnit);
            redisRepository.saveRefreshToken(memberId, refreshToken, expireTime, timeUnit);
        } catch (RedisConnectionException e) {
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }

    public String getRefreshToken(UUID memberId) {
        try {
            return redisRepository.getRefreshToken(memberId);
        } catch (RedisConnectionException e) {
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }

    public TimeUnit convertChronoUnitToTimeUnit(ChronoUnit chronoUnit) {
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
                throw GlobalErrorCode.UNSUPPORTED_TYPE.toException();
        }
    }
}