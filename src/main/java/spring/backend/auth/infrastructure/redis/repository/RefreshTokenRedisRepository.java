package spring.backend.auth.infrastructure.redis.repository;

import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import spring.backend.auth.domain.repository.RefreshTokenRepository;
import spring.backend.core.exception.error.GlobalErrorCode;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Log4j2
public class RefreshTokenRedisRepository implements RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String refreshToken, UUID memberId, Long expireTime, TimeUnit timeUnit) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(refreshToken, memberId.toString(), expireTime, timeUnit);
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 오류 : {}", e.getMessage());
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }

    @Override
    public String findByRefreshToken(String refreshToken) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            return valueOperations.get(refreshToken);
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 오류 : {}", e.getMessage());
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        try {
            redisTemplate.delete(refreshToken);
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 오류 : {}", e.getMessage());
            throw GlobalErrorCode.REDIS_CONNECTION_ERROR.toException();
        } catch (Exception e) {
            throw GlobalErrorCode.INTERNAL_ERROR.toException();
        }
    }
}
