package com.tonem.boombeene.crowdreport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "crowd_reports")
public class CrowdReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CongestionLevel level;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private CrowdReport(Long storeId, Long userId, CongestionLevel level) {
        this.storeId = storeId;
        this.userId = userId;
        this.level = level;
        this.createdAt = LocalDateTime.now();
    }

    public static CrowdReport create(Long storeId, Long userId, CongestionLevel level) {
        return new CrowdReport(storeId, userId, level);
    }
}
