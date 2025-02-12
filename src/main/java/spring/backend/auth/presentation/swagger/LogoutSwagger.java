package spring.backend.auth.presentation.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.core.configuration.swagger.ApiErrorCode;
import spring.backend.core.exception.error.GlobalErrorCode;
import spring.backend.core.presentation.RestResponse;

@Tag(name = "Auth", description = "인증/인가")
public interface LogoutSwagger {

    @Operation(
            summary = "로그아웃 API",
            description = "사용자의 로그아웃을 진행합니다. \n\n 로그아웃 시, 사용자의 토큰이 무효화되어, 다시 로그인을 진행해야 합니다.",
            operationId = "/v1/logout"
    )
    @ApiErrorCode({
            GlobalErrorCode.class, AuthenticationErrorCode.class
    })
    ResponseEntity<RestResponse<Void>> logout(
            @Parameter(description = "쿠키에 있는 access_token", required = false)
            String accessToken,
            @Parameter(description = "쿠키에 있는 refresh_token", required = false)
            String refreshToken
    );
}
