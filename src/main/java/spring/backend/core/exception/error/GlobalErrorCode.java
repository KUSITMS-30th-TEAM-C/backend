package spring.backend.core.exception.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode<RuntimeException> {

  ALREADY_PROCESS_STARTED(HttpStatus.BAD_REQUEST, "이미 처리중인 요청입니다."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 내부 오류입니다."),
  REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 서버와의 연결에 문제가 발생했습니다."),
  UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 타입입니다.");

  private final HttpStatus httpStatus;

  private final String message;

  @Override
  public RuntimeException toException() {
    return new RuntimeException(message);
  }
}
