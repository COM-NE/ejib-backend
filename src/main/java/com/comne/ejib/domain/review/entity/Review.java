package com.comne.ejib.domain.review.entity;

import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(length = 10)
    private String reviewType;

    private Integer residenceDuration;
    private Integer totalScore;
    private Integer houseScore;
    private Integer facilityScore;
    private Integer infraScore;
    private Integer safetyScore;
    private Integer envScore;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer deposit;
    private Integer monthlyRent;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewImage> images = new ArrayList<>();
}
