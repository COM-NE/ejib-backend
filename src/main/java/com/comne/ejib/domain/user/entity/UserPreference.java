package com.comne.ejib.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "user_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_preference", columnNames = {"user_id", "preference_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preference_id", nullable = false)
    private PreferenceCategory preference;

    private UserPreference(User user, PreferenceCategory preference) {
        this.user = user;
        this.preference = preference;
    }

    public static UserPreference of(User user, PreferenceCategory preference) {
        return new UserPreference(user, preference);
    }
}
