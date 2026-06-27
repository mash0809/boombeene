package com.tonem.boombeene.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long userId;

    @Column(nullable = false)
    private int balance;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private UserPoint(Long userId, int initialAmount) {
        this.userId = userId;
        this.balance = initialAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public static UserPoint create(Long userId, int initialAmount) {
        return new UserPoint(userId, initialAmount);
    }

    public void addBalance(int amount) {
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }
}
