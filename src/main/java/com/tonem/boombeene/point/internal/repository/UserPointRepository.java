package com.tonem.boombeene.point.internal.repository;

import com.tonem.boombeene.point.internal.entity.UserPoint;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);

    // 비관적 락 테스트를 위한 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select userPoint from UserPoint userPoint where userPoint.userId = :userId")
    Optional<UserPoint> findWithLockByUserId(@Param("userId") Long userId);
}
