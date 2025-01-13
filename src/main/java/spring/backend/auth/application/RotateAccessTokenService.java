package spring.backend.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.auth.infrastructure.redis.repository.RefreshTokenRedisRepository;
import spring.backend.auth.presentation.dto.response.RotateAccessTokenResponse;
import spring.backend.core.application.JwtService;
import spring.backend.member.domain.entity.Member;
import spring.backend.member.domain.repository.MemberRepository;
import spring.backend.member.exception.MemberErrorCode;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class RotateAccessTokenService {
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public RotateAccessTokenResponse rotateAccessToken(String refreshToken) {
        if(refreshToken == null) {
            throw AuthenticationErrorCode.MISSING_COOKIE_VALUE.toException();
        }
        refreshTokenService.validateRefreshToken(refreshToken);
        UUID memberId = UUID.fromString(refreshTokenRedisRepository.findByRefreshToken(refreshToken));
        Member member = memberRepository.findById(memberId);
        return new RotateAccessTokenResponse(jwtService.provideAccessToken(Optional.ofNullable(member).orElseThrow(MemberErrorCode.NOT_EXIST_MEMBER::toException)));
    }
}
