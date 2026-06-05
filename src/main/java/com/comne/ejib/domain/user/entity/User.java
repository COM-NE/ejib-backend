package com.comne.ejib.domain.user.entity;

import com.comne.ejib.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(nullable = false)
    private Integer profileImage; // 0: blue, 1: red, 2: yellow

    @Column(nullable = false, length = 20)
    private String jobType;

    @Column(nullable = false)
    private Integer point;

    @Column(nullable = false, length = 255, unique = true)
    private String kakaoId;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean onboardingCompleted = false;

    public void completeOnboarding(String name, String nickname, UserProfile profile, UserStatus status) {
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profile.code();
        this.jobType = status.value();
        this.onboardingCompleted = true;
    }

    public boolean isOnboardingCompleted() {
        return Boolean.TRUE.equals(onboardingCompleted);
    }

    public String getProfile() {
        UserProfile profile = UserProfile.fromCode(profileImage);
        return profile == null ? null : profile.value();
    }

    public String getStatus() {
        return jobType;
    }
}
