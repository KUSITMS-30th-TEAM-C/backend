package spring.backend.auth.dto.response;

import spring.backend.member.domain.value.Role;

public record LoginResponse(String accessToken, Role role) {
}
