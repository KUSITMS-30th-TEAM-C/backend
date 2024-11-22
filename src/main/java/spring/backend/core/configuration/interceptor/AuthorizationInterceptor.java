package spring.backend.core.configuration.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.core.application.JwtService;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    private static final List<String> PASS_THROUGH_PATTERNS = Arrays.asList(
            "/swagger-ui", "/v3/api-docs", "/v1/oauth", "/v1/token/rotate"
    );


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (isPassThroughRequest(request.getRequestURI())) {
            return true;
        }
        String accessToken = extractToken(request);
        if (accessToken == null) {
            log.error("쿠키에 토큰이 존재하지 않습니다.");
            throw AuthenticationErrorCode.NOT_EXIST_TOKEN.toException();
        }
        jwtService.validateTokenExpiration(accessToken);
        return true;
    }

    private boolean isPassThroughRequest(String uri) {
        return PASS_THROUGH_PATTERNS.stream().anyMatch(uri::contains);
    }

    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("cookie: {}", cookie);
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
