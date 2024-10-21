package spring.backend.auth.infrastructure.redis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import spring.backend.auth.domain.repository.RefreshTokenRepository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository implements RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(UUID memberId, String refreshToken, Long expireTime, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(memberId.toString(), refreshToken, expireTime, timeUnit);
    }

    @Override
    public String findByMemberId(UUID memberId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(memberId.toString());
    }

    @Override
    public void delete(UUID memberId) {
        redisTemplate.delete(memberId.toString());
    }
}
