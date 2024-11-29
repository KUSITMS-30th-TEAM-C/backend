package spring.backend.member.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import spring.backend.member.domain.repository.MemberRepository;
import spring.backend.member.domain.value.Role;

@Service
@RequiredArgsConstructor
@Log4j2
public class ValidateNicknameService {

    private final MemberRepository memberRepository;

    public boolean validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            log.error("[ValidateNicknameService] Nickname is empty");
            return false;
        }
        if (nickname.length() > 6) {
            log.error("[ValidateNicknameService] Nickname is smaller than 6 characters");
            return false;
        }
        if (!nickname.matches("^[a-zA-Z0-9가-힣ㄱ-ㅎ]{1,6}$")) {
            log.error("[ValidateNicknameService] Nickname is invalid");
            return false;
        }
        if (memberRepository.existsByNicknameAndRole(nickname, Role.MEMBER)) {
            log.error("[ValidateNicknameService] Nickname is already in use");
            return false;
        }
        return true;
    }
}
