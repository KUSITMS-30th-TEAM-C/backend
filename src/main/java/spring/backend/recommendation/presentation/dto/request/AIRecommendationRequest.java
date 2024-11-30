package spring.backend.recommendation.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import spring.backend.activity.domain.value.Keyword;
import spring.backend.activity.domain.value.Type;

public record AIRecommendationRequest(
        @NotNull(message = "자투리 시간은 필수 입력 항목입니다.")
        @Min(value = 10, message = "자투리 시간은 10부터 300 사이의 숫자로 입력해주세요.")
        @Max(value = 300, message = "자투리 시간은 10부터 300 사이의 숫자로 입력해주세요.")
        @Schema(description = "자투리 시간", example = "30")
        Integer spareTime,

        @NotNull(message = "활동 유형은 필수 입력 항목입니다.")
        @Schema(description = "활동 타입(ONLINE, OFFLINE, ONLINE_AND_OFFLINE 중 하나를 선택합니다.)", example = "OFFLINE")
        Type activityType,

        @NotNull(message = "키워드는 필수 입력 항목입니다.")
        @Schema(description = "활동 키워드", example = "[\"RELAXATION\",\"ENTERTAINMENT\"]")
        Keyword.Category[] keywords,

        @Schema(description = "위치(activityType이 OFFLINE, ONLINE_AND_OFFLINE인 경우에만 필요합니다.)", example = "서울시 마포구 공덕동")
        String location
) {
}
