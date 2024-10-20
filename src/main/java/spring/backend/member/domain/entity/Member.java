package spring.backend.member.domain.entity;

import lombok.Builder;
import lombok.Getter;
import spring.backend.member.domain.value.Provider;
import spring.backend.member.domain.value.Role;
import spring.backend.member.infrastructure.persistence.jpa.entity.MemberJpaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Member {

    private UUID id;

    private Provider provider;

    private Role role;

    private String email;

    private String nickname;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean deleted;

    public static Member toDomainEntity(MemberJpaEntity memberJpaEntity) {
        return Member.builder()
                .id(memberJpaEntity.getId())
                .provider(memberJpaEntity.getProvider())
                .role(memberJpaEntity.getRole())
                .email(memberJpaEntity.getEmail())
                .nickname(memberJpaEntity.getNickname())
                .createdAt(memberJpaEntity.getCreatedAt())
                .updatedAt(memberJpaEntity.getUpdatedAt())
                .deleted(memberJpaEntity.getDeleted())
                .build();
    }

    public boolean isSameProvider(Provider otherProvider) {
        return this.provider.equals(otherProvider);
    }

    public boolean isMember() {
        return Role.MEMBER.equals(this.role);
    }

    public static Member createGuestMember(Provider provider, String email, String nickname) {
        return Member.builder()
                .provider(provider)
                .role(Role.GUEST)
                .email(email)
                .nickname(nickname)
                .build();
    }
}
