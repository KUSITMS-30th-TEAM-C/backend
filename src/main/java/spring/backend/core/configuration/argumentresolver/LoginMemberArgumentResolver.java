package spring.backend.core.configuration.argumentresolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import spring.backend.core.application.JwtService;
import spring.backend.member.domain.entity.Member;
import spring.backend.member.domain.repository.MemberRepository;
import spring.backend.member.exception.MemberErrorCode;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String token = extractToken(webRequest);
        if (token == null) {
            log.error("토큰이 존재하지 않습니다.");
            throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
        }

        UUID memberId = jwtService.extractMemberId(token);
        Member member = memberRepository.findById(memberId);
        return Optional.ofNullable(member).orElseThrow(MemberErrorCode.NOT_EXIST_MEMBER::toException);
    }

    private String extractToken(NativeWebRequest request) {
        HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);

        if (httpRequest != null) {
            if (isSwaggerRequest(httpRequest)) {
                String authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
                if (authHeader != null && authHeader.startsWith(AUTHORIZATION_BEARER_PREFIX)) {
                    return authHeader.substring(7);
                }
            }

            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                return Arrays.stream(cookies)
                        .filter(cookie -> "access_token".equals(cookie.getName()))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
            }
        }
        return null;
    }

    private boolean isSwaggerRequest(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null && referer.contains("/swagger-ui");
    }
}
