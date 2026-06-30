package com.tonem.boombeene.point.internal.entity;

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
@Table(name = "point_ledger")
public class PointLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PointType type;

    @Column(nullable = false)
    private int amount;

    private Long reportId;

    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private PointLedger(
            Long userId,
            PointType type,
            int amount,
            Long reportId,
            String description,
            String idempotencyKey
    ) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.reportId = reportId;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }

    // EARN 타입 ledger 에 들어갈 멱등성 키 형식
    public static String earnKey(Long userId, Long reportId) {
        return "EARN_" + userId + "_" + reportId;
    }

    public static PointLedger earn(Long userId, Long reportId, int amount, String description) {
        return new PointLedger(userId, PointType.EARN, amount, reportId, description, earnKey(userId, reportId));
    }
}
