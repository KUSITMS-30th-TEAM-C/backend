package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.backend.auth.application.HandleOAuthLoginService;
import spring.backend.auth.presentation.dto.response.LoginResponse;
import spring.backend.auth.presentation.dto.response.LoginUserInfoResponse;
import spring.backend.core.configuration.argumentresolver.ClientIp;
import spring.backend.core.presentation.RestResponse;

@RestController
@RequestMapping("/v1/oauth/login")
@RequiredArgsConstructor
public class HandleOAuthLoginController {

    private final HandleOAuthLoginService handleOAuthLoginService;

    @GetMapping("/{providerName}")
    public ResponseEntity<RestResponse<LoginUserInfoResponse>> handleOAuthLogin(@RequestParam(value = "code", required = false) String code,
                                                                                @RequestParam(value = "state", required = false) String state, @PathVariable String providerName, @ClientIp String ip) {
        LoginResponse loginResponse = handleOAuthLoginService.handleOAuthLogin(providerName, code, state, ip);
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", loginResponse.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", loginResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        LoginUserInfoResponse loginUserInfoResponse = LoginUserInfoResponse.from(loginResponse);

        return ResponseEntity.ok()
                .headers(header -> {
                    header.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                    header.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                })
                .body(new RestResponse<>(loginUserInfoResponse));
    }
}
