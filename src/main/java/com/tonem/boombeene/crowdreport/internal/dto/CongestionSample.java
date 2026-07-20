package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import java.time.LocalDateTime;

public interface CongestionSample {

    CongestionLevel getLevel();

    LocalDateTime getCreatedAt();
}
