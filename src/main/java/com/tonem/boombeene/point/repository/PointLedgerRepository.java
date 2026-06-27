package com.tonem.boombeene.point.repository;

import com.tonem.boombeene.point.entity.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
