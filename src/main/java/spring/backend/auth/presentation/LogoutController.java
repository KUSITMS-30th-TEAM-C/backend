package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.backend.auth.application.RefreshTokenService;
import spring.backend.auth.presentation.swagger.LogoutSwagger;
import spring.backend.core.presentation.RestResponse;

@RestController
@RequiredArgsConstructor
public class LogoutController implements LogoutSwagger {
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/v1/logout")
    public ResponseEntity<RestResponse<Void>> logout(
            @CookieValue(name = "access_token", required = false) String accessToken,
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        refreshTokenService.deleteRefreshToken(refreshToken);
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }
}
