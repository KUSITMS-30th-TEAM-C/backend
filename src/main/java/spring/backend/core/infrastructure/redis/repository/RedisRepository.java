package spring.backend.core.infrastructure.redis.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(UUID memberId, String refreshToken, Long expireTime, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(memberId.toString(), refreshToken, expireTime, timeUnit);
    }

    public String getRefreshToken(UUID memberId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(memberId.toString());
    }
}