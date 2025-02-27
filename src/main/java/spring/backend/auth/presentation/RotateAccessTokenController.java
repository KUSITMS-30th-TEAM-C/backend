package spring.backend.auth.presentation;

import com.maxmind.geoip2.exception.GeoIp2Exception;
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
import spring.backend.auth.presentation.dto.response.RotateTokenResponse;
import spring.backend.auth.presentation.swagger.RotateTokenSwagger;
import spring.backend.core.configuration.argumentresolver.ClientIp;
import spring.backend.core.presentation.RestResponse;

import java.io.IOException;

import static org.springframework.http.ResponseCookie.from;

@RestController
@RequestMapping("/v1/token/rotate")
@RequiredArgsConstructor
@Log4j2
public class RotateAccessTokenController implements RotateTokenSwagger {
    private final RotateAccessTokenService rotateTokenService;

    @PostMapping
    public ResponseEntity<RestResponse<RotateTokenResponse>> rotateToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @ClientIp String ip
    ) throws IOException, GeoIp2Exception {
        RotateTokenResponse rotateTokenResponse = rotateTokenService.rotateToken(refreshToken, ip);
        ResponseCookie newAccessToken = from("access_token", rotateTokenResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        ResponseCookie newRefreshToken = from("refresh_token", rotateTokenResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessToken.toString())
                .header(HttpHeaders.SET_COOKIE, newRefreshToken.toString())
                .build();
    }
}
