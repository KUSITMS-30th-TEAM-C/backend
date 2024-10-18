package spring.backend.core.application;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import spring.backend.member.domain.entity.Member;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisServiceTest {
    @Autowired
    private RedisService redisService;

    @Autowired
    private JwtService jwtService;

    private final UUID memberId = UUID.randomUUID();

    @AfterAll
    static void afterAll(@Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        connectionFactory.getConnection().flushDb();
    }

    @DisplayName("RefreshToken이 발급될 때 ID와 RefreshToken를 Redis에 저장된다")
    @Test
    void saveRefreshTokenWhenTokenReleased() {
        // given
        Member member = Member.builder()
                .id(memberId)
                .email("test@test.com")
                .build();
        // when
        String refreshToken = jwtService.provideRefreshToken(member);
        // then
        assertThat(refreshToken).isEqualTo(redisService.getRefreshToken(member.getId()));
    }
}