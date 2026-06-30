package com.tonem.boombeene.point.internal.repository;

import com.tonem.boombeene.point.internal.entity.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
