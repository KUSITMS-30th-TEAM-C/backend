package spring.backend.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import spring.backend.auth.exception.AuthenticationErrorCode;
import spring.backend.auth.infrastructure.OAuthRestClient;
import spring.backend.auth.infrastructure.OAuthRestClientFactory;
import spring.backend.auth.presentation.dto.response.LoginResponse;
import spring.backend.auth.presentation.dto.response.OAuthAccessTokenResponse;
import spring.backend.auth.presentation.dto.response.OAuthResourceResponse;
import spring.backend.core.application.JwtService;
import spring.backend.member.domain.entity.Member;
import spring.backend.member.domain.service.CreateMemberWithOAuthService;
import spring.backend.member.domain.value.Provider;
import spring.backend.member.presentation.dto.request.CreateMemberWithOAuthRequest;

@Service
@RequiredArgsConstructor
@Log4j2
public class HandleOAuthLoginService {

    private final OAuthRestClientFactory oAuthRestClientFactory;

    private final CreateMemberWithOAuthService createMemberWithOAuthService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public LoginResponse handleOAuthLogin(String providerName, String code, String state, String ip) {
        if (providerName == null || providerName.isEmpty()) {
            throw AuthenticationErrorCode.NOT_EXIST_PROVIDER.toException();
        }
        Provider provider = Provider.valueOf(providerName.toUpperCase());
        OAuthRestClient oAuthRestClient = oAuthRestClientFactory.getOAuthRestClient(provider);

        OAuthAccessTokenResponse oAuthAccessTokenResponse = oAuthRestClient.getAccessToken(code, state);

        if (oAuthAccessTokenResponse == null) {
            log.error("[HandleOAuthLoginService] OAuth access token could not be retrieved.");
            throw AuthenticationErrorCode.ACCESS_TOKEN_NOT_ISSUED.toException();
        }

        OAuthResourceResponse oAuthResourceResponse = oAuthRestClient.getResource(oAuthAccessTokenResponse.getAccessToken());

        if (oAuthResourceResponse == null) {
            log.error("[HandleOAuthLoginService] OAuth resource could not be retrieved.");
            throw AuthenticationErrorCode.RESOURCE_SERVER_UNAVAILABLE.toException();
        }

        CreateMemberWithOAuthRequest createMemberWithOAuthRequest = CreateMemberWithOAuthRequest.builder()
                .provider(provider)
                .email(oAuthResourceResponse.getEmail())
                .build();

        Member member = createMemberWithOAuthService.createMemberWithOAuth(createMemberWithOAuthRequest);
        String accessToken = jwtService.provideAccessToken(member);
        String refreshToken = jwtService.provideRefreshToken(member, ip);
        refreshTokenService.saveRefreshToken(refreshToken, member);
        return LoginResponse.of(accessToken, refreshToken, member);
    }
}
