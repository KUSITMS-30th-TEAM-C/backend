package spring.backend.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ActivityWithTitleAndSavedTimeResponse(
        @Schema(description = "활동 제목", example = "마음의 편안을 가져다주는 명상음악 20분 듣기")
        String title,

        @Schema(description = "모은 시간", example = "60")
        long savedTime,

        @Schema(description = "활동 날짜", example = "2021-07-01T00:00:00")
        LocalDateTime dateOfActivity
) {
}
