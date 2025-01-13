package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.backend.auth.application.RotateAccessTokenService;
import spring.backend.auth.presentation.dto.response.RotateAccessTokenResponse;
import spring.backend.auth.presentation.swagger.RotateAccessTokenSwagger;
import spring.backend.core.presentation.RestResponse;

import static org.springframework.http.ResponseCookie.from;

@RestController
@RequestMapping("/v1/token/rotate")
@RequiredArgsConstructor
@Log4j2
public class RotateAccessTokenController implements RotateAccessTokenSwagger {
    private final RotateAccessTokenService rotateTokenService;

    @PostMapping
    public ResponseEntity<RestResponse<RotateAccessTokenResponse>> rotateAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        RotateAccessTokenResponse rotateAccessTokenResponse = rotateTokenService.rotateAccessToken(refreshToken);
        ResponseCookie newAccessToken = from("access_token", rotateAccessTokenResponse.accessToken())
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessToken.toString())
                .build();
    }
}
