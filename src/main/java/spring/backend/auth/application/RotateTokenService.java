package spring.backend.auth.application;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring.backend.auth.dto.response.RotateTokenResponse;
import spring.backend.core.application.JwtService;
import spring.backend.member.application.MemberService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RotateTokenService {
    private final MemberService memberService;
    private final JwtService jwtService;

    public RotateTokenResponse rotateAccessToken(String refreshToken) {
        UUID memberId = UUID.fromString(jwtService.getPayload(refreshToken).get("memberId", String.class));
        return new RotateTokenResponse(jwtService.provideAccessToken(memberService.findByMemberId(memberId)));
    }
}
