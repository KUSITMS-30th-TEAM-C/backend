package spring.backend.core.configuration.argumentresolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import spring.backend.core.application.JwtService;
import spring.backend.member.domain.entity.Member;
import spring.backend.member.domain.repository.MemberRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoginMemberArgumentResolverTest {

    @InjectMocks
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Mock
    private JwtService jwtService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private ModelAndViewContainer mavContainer;

    private UUID memberId;
    private String token;
    private Member member;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        memberId = UUID.randomUUID();
        member = Member.builder()
                .id(memberId)
                .build();
        token = "mockToken";
        when(jwtService.provideAccessToken(any(Member.class))).thenReturn(token);
    }

    @DisplayName("LoginMember 어노테이션이 있는 경우 지원한다")
    @Test
    public void supportsParameterReturnsTrueForLoginMember() {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.hasParameterAnnotation(LoginMember.class)).thenReturn(true);
        Assertions.assertTrue(loginMemberArgumentResolver.supportsParameter(parameter));
    }

    @DisplayName("쿠키에 유효한 토큰이 있을 때 Member 객체를 반환한다")
    @Test
    public void returnsMemberObject_whenValidTokenInCookie() throws Exception {
        // given
        String cookieName = "access_token";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        System.out.println("token: " + token);
        System.out.println("member : " + member);
        mockRequest.setCookies(new Cookie(cookieName, token));

        // when
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.hasParameterAnnotation(LoginMember.class)).thenReturn(true);
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(mockRequest);
        when(jwtService.extractMemberId(token)).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(member);

        // then
        Object result = loginMemberArgumentResolver.resolveArgument(parameter, mavContainer, webRequest, null);
        assertNotNull(result);
        assertThat(result).isInstanceOf(Member.class);
        assertThat(((Member) result).getId()).isEqualTo(memberId);
    }
}
