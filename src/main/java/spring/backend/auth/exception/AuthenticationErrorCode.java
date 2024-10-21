package spring.backend.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import spring.backend.core.exception.DomainException;
import spring.backend.core.exception.error.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthenticationErrorCode implements BaseErrorCode<DomainException> {

    NOT_EXIST_HEADER(HttpStatus.UNAUTHORIZED, "Authorization Header가 존재하지 않습니다."),
    NOT_EXIST_TOKEN(HttpStatus.UNAUTHORIZED, "Authorization Header에 Token이 존재하지 않습니다."),
    NOT_MATCH_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "토큰의 형식이 맞지 않습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "토큰의 서명이 올바르지 않습니다."),
    NOT_DEFINE_TOKEN(HttpStatus.UNAUTHORIZED, "정의되지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "유효한 OAuth 써드파티 제공자가 아닙니다."),
    NOT_EXIST_PROVIDER(HttpStatus.BAD_REQUEST, "OAuth 써드파티 제공자가 존재하지 않습니다."),
    NOT_EXIST_AUTH_CODE(HttpStatus.BAD_GATEWAY, "OAuth 써드파티 제공자에서 제공받은 인증 코드가 존재하지 않습니다."),
    ACCESS_TOKEN_NOT_ISSUED(HttpStatus.BAD_GATEWAY, "OAuth 써드파티 제공자에서 액세스 토큰이 발급되지 않았습니다."),
    NOT_EXIST_RESOURCE_RESPONSE(HttpStatus.BAD_GATEWAY, "OAuth 써드파티 리소스 서버에서 자원이 존재하지 않습니다."),
    RESOURCE_SERVER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "OAuth Resource Server에 접근할 수 없습니다."),
    UNSUPPORTED_REDIS_TIME_TYPE(HttpStatus.BAD_REQUEST, "Redis 만료시간은 ChronoUnit 타입이어야 합니다."),
    MISMATCH_TOKEN_MEMBER(HttpStatus.UNAUTHORIZED, "토큰의 회원 ID와 요청한 회원 ID가 일치하지 않습니다."),
    NOT_EXIST_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 저장소에 존재하지 않습니다.");


    private final HttpStatus httpStatus;

    private final String message;

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}