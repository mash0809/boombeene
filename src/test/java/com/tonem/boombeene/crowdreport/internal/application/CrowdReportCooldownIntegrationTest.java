package com.tonem.boombeene.crowdreport.internal.application;

import com.tonem.boombeene.AbstractIntegrationTest;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.internal.repository.CrowdReportRepository;
import com.tonem.boombeene.store.StoreApi;
import com.tonem.boombeene.store.StoreInfo;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
class CrowdReportCooldownIntegrationTest extends AbstractIntegrationTest {

    private static final long USER_ID = 20_000L;
    private static final long STORE_ID = 30_000L;
    private static final int THREAD_COUNT = 50;

    @Autowired
    private CrowdReportService crowdReportService;

    @Autowired
    private CrowdReportRepository crowdReportRepository;

    @MockitoBean
    private StoreApi storeApi;

    @Test
    @DisplayName("쿨다운 중 동시 요청이 와도 단 하나의 요청만 성공한다")
    void reportAllowsOnlyOneConcurrentRequestDuringCooldown() throws Exception {
        when(storeApi.getById(STORE_ID)).thenReturn(new StoreInfo(STORE_ID, 37.5662952, 126.9779451));
        var request = new CrowdReportRequest(
                STORE_ID,
                37.5662952,
                126.9779451,
                0.0,
                CongestionLevel.NORMAL
        );
        var readyLatch = new CountDownLatch(THREAD_COUNT);
        var startLatch = new CountDownLatch(1);
        var executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        var futures = new ArrayList<Future<Object>>();

        try {
            // 모든 요청이 같은 시점에 마커 획득을 시도하도록 출발 시점을 맞춘다.
            for (int i = 0; i < THREAD_COUNT; i++) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();
                    try {
                        return crowdReportService.report(USER_ID, request);
                    } catch (RuntimeException e) {
                        return e;
                    }
                }));
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            var results = new ArrayList<>();
            for (var future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }

            long successCount = results.stream()
                    .filter(CrowdReportDto.class::isInstance)
                    .count();
            long cooldownCount = results.stream()
                    .filter(CooldownActiveException.class::isInstance)
                    .count();
            long savedReportCount = crowdReportRepository.findAll().stream()
                    .filter(report -> report.getUserId().equals(USER_ID))
                    .filter(report -> report.getStoreId().equals(STORE_ID))
                    .count();

            assertThat(successCount).isEqualTo(1);
            assertThat(cooldownCount).isEqualTo(THREAD_COUNT - 1);
            assertThat(results).hasSize((int) (successCount + cooldownCount));
            assertThat(savedReportCount).isEqualTo(1);
        } finally {
            executorService.shutdownNow();
        }
    }
}
