package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.backend.auth.application.RefreshTokenService;
import spring.backend.auth.presentation.swagger.LogoutSwagger;
import spring.backend.core.application.JwtService;

import java.util.UUID;
import spring.backend.auth.presentation.swagger.LogoutSwagger;
import spring.backend.core.configuration.argumentresolver.AuthorizedMember;
import spring.backend.core.configuration.interceptor.Authorization;
import spring.backend.member.domain.entity.Member;

@RestController
@RequiredArgsConstructor
public class LogoutController implements LogoutSwagger {
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/v1/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "access_token", required = false) String accessToken
    ) {

        UUID memberId = jwtService.extractMemberIdFromExpiredAccessToken(accessToken);
        refreshTokenService.deleteRefreshToken(memberId);

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie userRoleCookie = ResponseCookie.from("user_role", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, userRoleCookie.toString())
                .build();
    }
}
