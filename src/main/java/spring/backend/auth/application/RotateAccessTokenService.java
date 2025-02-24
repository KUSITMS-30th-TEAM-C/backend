package spring.backend.auth.application;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.auth.infrastructure.redis.repository.RefreshTokenRedisRepository;
import spring.backend.auth.presentation.dto.response.RotateTokenResponse;
import spring.backend.core.application.GeoLocationService;
import spring.backend.core.application.JwtService;
import spring.backend.member.domain.entity.Member;
import spring.backend.member.domain.repository.MemberRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class RotateAccessTokenService {
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final GeoLocationService geoLocationService;

    public RotateTokenResponse rotateToken(String refreshToken, String newIp) throws IOException, GeoIp2Exception {
        if (refreshToken == null) {
            throw AuthenticationErrorCode.MISSING_COOKIE_VALUE.toException();
        }
        refreshTokenService.validateRefreshToken(refreshToken);

        String savedIp = jwtService.getPayload(refreshToken).get("ip", String.class);

        if (geoLocationService.checkUserLocation(newIp, savedIp)) {
            refreshTokenService.deleteRefreshToken(refreshToken);
            log.error("유효하지 않은 위치에서 토큰 재발급을 시도했습니다.");
            throw AuthenticationErrorCode.TOKEN_ROTATE_ATTEMPT_FROM_INVALID_LOCATION.toException();
        }

        UUID memberId = UUID.fromString(refreshTokenRedisRepository.findByRefreshToken(refreshToken));
        Member member = memberRepository.findById(memberId);
        String newAccessToken = jwtService.provideAccessToken(member);
        String newRefreshToken = jwtService.provideRefreshToken(member, newIp);
        refreshTokenService.saveRefreshToken(newRefreshToken, member);
        return new RotateTokenResponse(
                newAccessToken,
                newRefreshToken
        );
    }
}
