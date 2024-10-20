package spring.backend.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.backend.auth.application.RotateTokenService;
import spring.backend.auth.dto.response.RotateTokenResponse;
import spring.backend.core.presentation.RestResponse;

@RestController
@RequestMapping("/v1/token/rotate")
@RequiredArgsConstructor
public class RotateTokenController {
    private final RotateTokenService rotateTokenService;

    @GetMapping
    public ResponseEntity<RestResponse<RotateTokenResponse>> rotateAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        return ResponseEntity.ok(new RestResponse<>(rotateTokenService.rotateAccessToken(refreshToken)));
    }
}
