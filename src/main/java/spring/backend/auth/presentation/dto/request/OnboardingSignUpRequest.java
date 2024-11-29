package spring.backend.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import spring.backend.member.domain.value.Gender;

public record OnboardingSignUpRequest(

        @Pattern(regexp = "^[a-zA-Z0-9가-힣ㄱ-ㅎ]{1,6}$", message = "닉네임은 한글, 영문, 숫자 조합 6자 이내로 입력해주세요.")
        @Schema(description = "닉네임", example = "조각조각")
        String nickname,

        @Schema(description = "출생년도", example = "2001")
        int birthYear,

        @NotNull(message = "성별을 입력해주세요.")
        @Schema(description = "성별 (MALE, FEMALE, NONE)", example = "FEMALE")
        Gender gender,

        @NotBlank(message = "프로필 이미지를 선택해주세요.")
        @Schema(description = "프로필 이미지", example = "http://test.jpg")
        String profileImage
) {
}
