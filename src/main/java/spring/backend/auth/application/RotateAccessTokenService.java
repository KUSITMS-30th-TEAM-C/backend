package spring.backend.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import spring.backend.auth.presentation.dto.response.RotateAccessTokenResponse;
import spring.backend.auth.exception.AuthenticationErrorCode;
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

    public RotateAccessTokenResponse rotateAccessToken(String invalidAccessToken) {
        UUID memberIdFromInvalidAccessToken = jwtService.extractMemberIdFromExpiredAccessToken(invalidAccessToken);
        String refreshToken = refreshTokenService.getRefreshToken(memberIdFromInvalidAccessToken);
        UUID memberId = extractMemberIdFromRefreshToken(refreshToken);
        validateRefreshToken(memberId, refreshToken);
        Member member = memberRepository.findById(memberId);
        return new RotateAccessTokenResponse(jwtService.provideAccessToken(Optional.ofNullable(member).orElseThrow(MemberErrorCode.NOT_EXIST_MEMBER::toException)));
    }

    private UUID extractMemberIdFromRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            log.error("쿠키에 refreshToken이 존재하지 않습니다.");
            throw AuthenticationErrorCode.MISSING_COOKIE_VALUE.toException();
        }
        return UUID.fromString(jwtService.getPayload(refreshToken).get("memberId", String.class));
    }

    private void validateRefreshToken(UUID memberId, String refreshToken) {
        String savedRefreshToken = refreshTokenService.getRefreshToken(memberId);
        if (!savedRefreshToken.equals(refreshToken)) {
            log.error("리프레시 토큰이 저장소에 존재하지 않습니다.");
            throw AuthenticationErrorCode.NOT_EXIST_REFRESH_TOKEN.toException();
        }
    }
}
