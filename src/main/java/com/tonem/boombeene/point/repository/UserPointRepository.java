package com.tonem.boombeene.point.repository;

import com.tonem.boombeene.point.entity.UserPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);
}
