package spring.backend.auth.presentation;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.Duration;

import static org.springframework.http.ResponseCookie.from;

@RestController
@RequestMapping("/v1/token/rotate")
@Log4j2
public class RotateAccessTokenController implements RotateTokenSwagger {
    private final RotateAccessTokenService rotateTokenService;
    private final long ACCESS_EXPIRATION;
    private final long REFRESH_EXPIRATION;

    public RotateAccessTokenController(RotateAccessTokenService rotateTokenService,
                                       @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
                                       @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry
    ) {
        this.rotateTokenService = rotateTokenService;
        this.ACCESS_EXPIRATION = accessTokenExpiry;
        this.REFRESH_EXPIRATION = refreshTokenExpiry;
    }

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
                .maxAge(Duration.ofSeconds(ACCESS_EXPIRATION))
                .path("/")
                .build();

        ResponseCookie newRefreshToken = from("refresh_token", rotateTokenResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(REFRESH_EXPIRATION))
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessToken.toString())
                .header(HttpHeaders.SET_COOKIE, newRefreshToken.toString())
                .build();
    }
}
