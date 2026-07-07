package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.crowdreport.CrowdReportCompleted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointEventListenerTest {

    @Mock
    private PointEarningService pointEarningService;

    @InjectMocks
    private PointEventListener pointEventListener;

    @Test
    void onCrowdReportDelegatesPointEarning() {
        var event = new CrowdReportCompleted(100L, 10L, 1L);

        pointEventListener.onCrowdReport(event);

        verify(pointEarningService).earnForCrowdReport(10L, 100L);
    }
}
