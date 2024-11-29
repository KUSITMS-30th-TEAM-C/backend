package spring.backend.quickstart.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import spring.backend.activity.domain.value.Type;

public record QuickStartRequest(

        @NotNull(message = "이름은 필수 입력 항목입니다.")
        @Pattern(regexp = "^(?!\\s)([a-zA-Z0-9가-힣ㄱ-ㅎ]+(\\s[a-zA-Z0-9가-힣ㄱ-ㅎ]+)*)?$", message = "이름은 한글(초성 포함), 영문, 숫자 및 공백만 입력 가능하며, 공백으로 시작하거나 끝날 수 없고, 연속된 공백이 없어야 합니다.")
        @Size(max = 10, message = "최대 10자까지 입력 가능합니다.")
        @Schema(description = "빠른 시작 이름", example = "등교")
        String name,

        @NotNull(message = "시작 시간의 시간은 필수 입력 항목입니다.")
        @Min(value = 0, message = "시작 시간의 시간은 최소 0이어야 합니다.")
        @Max(value = 12, message = "시작 시간의 시간은 최대 12이어야 합니다.")
        @Schema(description = "시작 시간의 시간", example = "12")
        Integer hour,

        @NotNull(message = "시작 시간의 분은 필수 입력 항목입니다.")
        @Min(value = 0, message = "시작 시간의 분은 최소 0이어야 합니다.")
        @Max(value = 59, message = "시작 시간의 분은 최대 59이어야 합니다.")
        @Schema(description = "시작 시간의 분", example = "30")
        Integer minute,

        @NotNull(message = "오전/오후 표시는 필수 입력 항목입니다.")
        @Pattern(regexp = "^(오전|오후)$", message = "meridiem은 '오전' 또는 '오후'여야 합니다.")
        @Schema(description = "오전/오후 표시", example = "오후")
        String meridiem,

        @NotNull(message = "자투리 시간은 필수 입력 항목입니다.")
        @Min(value = 10, message = "자투리 시간은 최소 10이어야 합니다.")
        @Max(value = 300, message = "자투리 시간은 최대 300이어야 합니다.")
        @Schema(description = "자투리 시간", example = "300")
        Integer spareTime,

        @NotNull(message = "활동 유형은 필수 입력 항목입니다.")
        @Schema(description = "활동 유형 (ONLINE, OFFLINE, ONLINE_AND_OFFLINE)", example = "ONLINE")
        Type type
) {
}
