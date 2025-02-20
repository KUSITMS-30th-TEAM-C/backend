package spring.backend.auth.presentation.dto.response;

public record RotateTokenResponse(String accessToken, String refreshToken) {
}
