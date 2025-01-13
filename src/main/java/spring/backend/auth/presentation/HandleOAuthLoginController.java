package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.backend.auth.application.HandleOAuthLoginService;
import spring.backend.auth.presentation.dto.response.LoginResponse;
import spring.backend.core.presentation.RestResponse;

@RestController
@RequestMapping("/v1/oauth/login")
@RequiredArgsConstructor
public class HandleOAuthLoginController {

    private final HandleOAuthLoginService handleOAuthLoginService;

    @GetMapping("/{providerName}")
    public ResponseEntity<?> handleOAuthLogin(@RequestParam(value = "code", required = false) String code,
                                              @RequestParam(value = "state", required = false) String state, @PathVariable String providerName) {
        LoginResponse loginResponse = handleOAuthLoginService.handleOAuthLogin(providerName, code, state);
        // Todo: 배포 시 httpOnly(true)로 변경
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", loginResponse.accessToken())
                .httpOnly(false)
                .path("/")
                .build();
        // Todo: 배포 시 httpOnly(true)로 변경
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", loginResponse.refreshToken())
                .httpOnly(false)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .headers(header -> {
                    header.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                    header.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                })
                .body(new RestResponse<>(loginResponse.userInfo()));
    }
}
