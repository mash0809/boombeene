package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.crowdreport.CrowdReportCompleted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    @ApplicationModuleListener
    public void onCrowdReport(CrowdReportCompleted event) {
        pointService.updateBalance(event.userId(), event.reportId());
    }
}
