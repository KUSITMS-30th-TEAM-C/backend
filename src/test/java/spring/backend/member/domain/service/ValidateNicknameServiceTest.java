package spring.backend.member.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spring.backend.member.domain.repository.MemberRepository;
import spring.backend.member.domain.value.Role;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidateNicknameServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ValidateNicknameService validateNicknameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("닉네임이 공백일 때 예외가 발생한다.")
    void throwExceptionWhenNicknameIsBlank() {
        // Given
        String nickname = " ";

        // When & Then
        assertFalse(validateNicknameService.validateNickname(nickname));
    }

    @Test
    @DisplayName("닉네임 길이가 6자를 초과할 때 예외가 발생한다.")
    void throwExceptionWhenNicknameLengthIsInvalid() {
        // Given
        String nickname = "1234567";

        // When & Then
        assertFalse(validateNicknameService.validateNickname(nickname));
    }

    @Test
    @DisplayName("이미 등록된 닉네임일 경우 예외가 발생한다.")
    void throwExceptionWhenNicknameIsAlreadyRegistered() {
        // Given
        String nickname = "등록된이름";
        when(memberRepository.existsByNicknameAndRole(nickname, Role.MEMBER)).thenReturn(true);

        // When & Then
        assertFalse(validateNicknameService.validateNickname(nickname));

        // Mock 객체 정상 동작 확인
        verify(memberRepository).existsByNicknameAndRole(nickname, Role.MEMBER);
    }

    @ParameterizedTest
    @DisplayName("올바른 형식의 이름일 경우 성공한다.")
    @ValueSource(strings = {"ㅍ카칩", "ㄱ", "포ㅋ칩", "포카ㅊ", "조각조각ㅈㄱ", "q", "qwerty"})
    void validateNicknameWithInitialConsonants(String nickname) {
        // When & Then
        assertTrue(validateNicknameService.validateNickname(nickname));
    }
}
