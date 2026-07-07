package com.tonem.boombeene.point.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_points")
public class UserPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    // 낙관적 락 테스트용
    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private UserPoint(Long userId) {
        this.userId = userId;
        this.balance = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserPoint create(Long userId) {
        return new UserPoint(userId);
    }

    public void addBalance(int amount) {
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }
}
