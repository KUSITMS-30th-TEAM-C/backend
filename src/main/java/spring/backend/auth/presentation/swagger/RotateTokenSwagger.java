package spring.backend.auth.presentation.swagger;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.auth.presentation.dto.response.RotateTokenResponse;
import spring.backend.core.configuration.swagger.ApiErrorCode;
import spring.backend.core.exception.error.GlobalErrorCode;
import spring.backend.core.presentation.RestResponse;

import java.io.IOException;

@Tag(name = "Auth", description = "인증/인가")
public interface RotateTokenSwagger {
    @Operation(
            summary = "토큰 재발급 API",
            description = "Access Token이 만료된 경우, Refresh Token을 이용하여 새로운 Access Token을 발급합니다",
            operationId = "/v1/token/rotate"
    )
    @ApiErrorCode({GlobalErrorCode.class, AuthenticationErrorCode.class})
    ResponseEntity<RestResponse<RotateTokenResponse>> rotateToken(
            @Parameter(description = "쿠키에 있는 refresh_token", required = false)
            String refreshToken,
            @Parameter(hidden = true) String ip
    ) throws IOException, GeoIp2Exception;
}
