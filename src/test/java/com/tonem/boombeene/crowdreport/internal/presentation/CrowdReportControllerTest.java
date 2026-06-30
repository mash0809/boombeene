package com.tonem.boombeene.crowdreport.internal.presentation;

import com.tonem.boombeene.crowdreport.internal.application.CrowdReportService;
import com.tonem.boombeene.crowdreport.internal.dto.CongestionResult;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.presentation.CrowdReportController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrowdReportControllerTest {

    @Mock
    private CrowdReportService crowdReportService;

    @InjectMocks
    private CrowdReportController crowdReportController;

    @Test
    void reportCreatesResponseFromServiceDto() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL);
        when(crowdReportService.report(10L, request)).thenReturn(new CrowdReportDto(99L));

        var response = crowdReportController.report(10L, request);

        assertThat(response.reportId()).isEqualTo(99L);
    }

    @Test
    void getCongestionCreatesResponseWithReportCount() {
        when(crowdReportService.getCongestion(1L, 37.5662952, 126.9779451))
                .thenReturn(CongestionResult.of(CongestionLevel.CROWDED, 3, 10.5));

        var response = crowdReportController.getCongestion(1L, 37.5662952, 126.9779451);

        assertThat(response.storeId()).isEqualTo(1L);
        assertThat(response.level()).isEqualTo(CongestionLevel.CROWDED);
        assertThat(response.count()).isEqualTo(3);
        assertThat(response.distanceMeters()).isEqualTo(10.5);
    }
}
