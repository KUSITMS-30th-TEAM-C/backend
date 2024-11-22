package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.backend.auth.application.HandleOAuthLoginService;
import spring.backend.auth.presentation.dto.response.LoginResponse;
import spring.backend.core.presentation.RestResponse;
import spring.backend.auth.dto.response.LoginResponse;

@RestController
@RequestMapping("/v1/oauth/login")
@RequiredArgsConstructor
public class HandleOAuthLoginController {

    private final HandleOAuthLoginService handleOAuthLoginService;

    @GetMapping("/{providerName}")
    public ResponseEntity<?> handleOAuthLogin(@RequestParam(value = "code", required = false) String code,
                                              @RequestParam(value = "state", required = false) String state, @PathVariable String providerName) {
        LoginResponse loginResponse = handleOAuthLoginService.handleOAuthLogin(providerName, code, state);
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", loginResponse.accessToken())
                .httpOnly(true)
                .build();

        ResponseCookie roleCookie = ResponseCookie.from("user_role", loginResponse.role().toString())
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, roleCookie.toString())
                .build();
    }
}
