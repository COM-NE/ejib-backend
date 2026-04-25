package com.comne.ejib.domain.property.entity;

import com.comne.ejib.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "properties")
public class Property extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyType;
    private String transactionType;
    private Integer monthlyRent;
    private Integer deposit;
    private Integer floor;

    @Column(precision = 10, scale = 2)
    private BigDecimal area;

    private String agency;
    private Integer distanceToSchool;
    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 13, scale = 10)
    private BigDecimal latitude;

    @Column(precision = 13, scale = 10)
    private BigDecimal longitude;
}
