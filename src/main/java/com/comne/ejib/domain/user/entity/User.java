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

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(nullable = false)
    private Integer profileImage; // TINYINT

    @Column(nullable = false, length = 20)
    private String jobType;

    @Column(nullable = false)
    private Integer point;

    @Column(nullable = false, length = 255, unique = true)
    private String kakaoId;
}
