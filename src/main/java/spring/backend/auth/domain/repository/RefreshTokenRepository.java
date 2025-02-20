package spring.backend.auth.domain.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface RefreshTokenRepository {
    void save(String refreshToken, UUID memberId, Long expireTime, TimeUnit timeUnit);
    String findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
    void deleteAll();
}
