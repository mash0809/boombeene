package com.tonem.boombeene.crowdreport.internal.repository;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.entity.CrowdReport;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrowdReportRepository extends JpaRepository<CrowdReport, Long> {

    boolean existsByUserIdAndStoreIdAndCreatedAtAfter(Long userId, Long storeId, LocalDateTime cutoff);

    @Query("SELECT c.level FROM CrowdReport c WHERE c.storeId = :storeId AND c.createdAt > :cutoff")
    List<CongestionLevel> findLevelsByStoreIdAndCreatedAtAfter(
            @Param("storeId") Long storeId,
            @Param("cutoff") LocalDateTime cutoff
    );
}
