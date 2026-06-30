package com.tonem.boombeene.point.internal.repository;

import com.tonem.boombeene.point.internal.entity.UserPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);
}
