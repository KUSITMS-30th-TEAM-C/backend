package spring.backend.quickstart.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spring.backend.activity.domain.value.Type;
import spring.backend.quickstart.presentation.dto.request.QuickStartRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuickStartRequestTest {

    private final Validator validator;

    public QuickStartRequestTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("name 필드 검증 테스트")
    class NameValidationTests {

        @Test
        @DisplayName("null 값일 경우 에러가 발생한다.")
        void whenNameIsNull_thenValidationFails() {
            QuickStartRequest request = new QuickStartRequest(null, 12, 30, "오전", 300, Type.OFFLINE);
            Set<ConstraintViolation<QuickStartRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(violation -> violation.getMessage().contains("이름은 필수 입력 항목입니다."));
        }

        @ParameterizedTest
        @DisplayName("올바른 형식의 이름일 경우 성공한다.")
        @ValueSource(strings = {"등교", "이름테스트", "띄어쓰기 포함 10", "사용자1", "ㄱ", "ㄱ나다라ㅁ바사ㅇㅈㅋ"})
        void whenNameIsValid_thenValidationSucceeds(String name) {
            QuickStartRequest request = new QuickStartRequest(name, 12, 30, "오전", 300, Type.OFFLINE);
            Set<ConstraintViolation<QuickStartRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @DisplayName("형식에 맞지 않는 이름일 경우 에러가 발생한다.")
        @ValueSource(strings = {" 이름", "이름 ", "이름@이름", "공백  공백"})
        void whenNameIsInvalid_thenValidationFails(String name) {
            QuickStartRequest request = new QuickStartRequest(name, 12, 30, "오전", 300, Type.OFFLINE);
            Set<ConstraintViolation<QuickStartRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(violation -> violation.getMessage().contains("이름은 한글(초성 포함), 영문, 숫자 및 공백만 입력 가능하며,"));
        }

        @Test
        @DisplayName("10자를 초과하는 경우 에러가 발생한다.")
        void whenNameExceedsMaxLength_thenValidationFails() {
            String name = "매우몹시너무긴이름longname";
            QuickStartRequest request = new QuickStartRequest(name, 12, 30, "오전", 300, Type.OFFLINE);
            Set<ConstraintViolation<QuickStartRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(violation -> violation.getMessage().contains("최대 10자까지 입력 가능합니다."));
        }
    }
}
