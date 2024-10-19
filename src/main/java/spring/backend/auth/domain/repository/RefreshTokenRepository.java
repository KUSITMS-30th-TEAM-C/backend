package spring.backend.auth.domain.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface RefreshTokenRepository {
    void save(UUID memberId, String refreshToken, Long expireTime, TimeUnit timeUnit);
    String findById(UUID memberId);
}
