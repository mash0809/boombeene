package com.tonem.boombeene.crowdreport.internal.presentation;

import com.tonem.boombeene.crowdreport.internal.application.CrowdReportService;
import com.tonem.boombeene.crowdreport.internal.dto.CongestionResult;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.internal.dto.HourlyCongestion;
import com.tonem.boombeene.crowdreport.internal.dto.WeeklyCongestionResult;
import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.presentation.CrowdReportController;
import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("서비스 DTO로부터 제보 응답을 생성한다")
    void reportCreatesResponseFromServiceDto() {
        var request = new CrowdReportRequest(
                1L,
                37.5662952,
                126.9779451,
                0.0,
                CongestionLevel.NORMAL,
                "좌석이 여유로워요"
        );
        when(crowdReportService.report(10L, request)).thenReturn(new CrowdReportDto(99L));

        var response = crowdReportController.report(10L, request);

        assertThat(response.reportId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("제보 건수를 포함한 혼잡도 응답을 생성한다")
    void getCongestionCreatesResponseWithReportCount() {
        when(crowdReportService.getCongestion(1L, 37.5662952, 126.9779451))
                .thenReturn(CongestionResult.of(
                        CongestionLevel.CROWDED,
                        3,
                        10.5,
                        List.of("줄이 길어요", "곧 자리가 날 것 같아요")
                ));

        var response = crowdReportController.getCongestion(1L, 37.5662952, 126.9779451);

        assertThat(response.storeId()).isEqualTo(1L);
        assertThat(response.level()).isEqualTo(CongestionLevel.CROWDED);
        assertThat(response.count()).isEqualTo(3);
        assertThat(response.distanceMeters()).isEqualTo(10.5);
        assertThat(response.comments()).containsExactly("줄이 길어요", "곧 자리가 날 것 같아요");
    }

    @Test
    @DisplayName("서비스 DTO로부터 요일별 혼잡도 응답을 생성한다")
    void getWeeklyCongestionCreatesResponsesFromServiceDtos() {
        var hourly = List.of(new HourlyCongestion(13, CongestionLevel.CROWDED, 3));
        when(crowdReportService.getWeeklyCongestion(1L)).thenReturn(List.of(
                new WeeklyCongestionResult(DayOfWeek.MONDAY, CongestionLevel.CROWDED, 3, hourly)
        ));

        var responses = crowdReportController.getWeeklyCongestion(1L);

        assertThat(responses).hasSize(1);
        var response = responses.getFirst();
        assertThat(response.storeId()).isEqualTo(1L);
        assertThat(response.day()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(response.level()).isEqualTo(CongestionLevel.CROWDED);
        assertThat(response.count()).isEqualTo(3);
        assertThat(response.hourly()).containsExactlyElementsOf(hourly);
    }
}
