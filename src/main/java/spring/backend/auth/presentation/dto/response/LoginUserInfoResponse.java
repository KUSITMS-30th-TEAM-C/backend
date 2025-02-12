package spring.backend.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import spring.backend.member.domain.value.Gender;
import spring.backend.member.domain.value.Role;

import java.time.LocalDateTime;

public record LoginUserInfoResponse(
        @Schema(description = "사용자 유형(MEMBER, GUEST)", example = "MEMBER")
        Role role,

        @Schema(description = "사용자 이메일", example = "example@example.com")
        String email,

        @Schema(description = "사용자 닉네임", example = "john_doe")
        String nickname,

        @Schema(description = "사용자 출생 연도", example = "1990")
        int birthYear,

        @Schema(description = "사용자 성별 (MALE, FEMALE, NONE)", example = "MALE")
        Gender gender,

        @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImage,

        @Schema(description = "사용자 가입 날짜", example = "2024-11-14T06:10:55.091954")
        LocalDateTime registrationDate
) {
    public static LoginUserInfoResponse from(LoginResponse loginResponse) {
        return new LoginUserInfoResponse(
                loginResponse.userInfo().role(),
                loginResponse.userInfo().email(),
                loginResponse.userInfo().nickname(),
                loginResponse.userInfo().birthYear(),
                loginResponse.userInfo().gender(),
                loginResponse.userInfo().profileImage(),
                loginResponse.userInfo().registrationDate()
                );
    }
}
