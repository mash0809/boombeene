package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.AbstractIntegrationTest;
import com.tonem.boombeene.point.internal.entity.PointLedger;
import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.PointLedgerRepository;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class PointServiceLockIntegrationTest extends AbstractIntegrationTest {

    private static final long USER_ID = 10_000L;
    private static final int THREAD_COUNT = 50;
    private static final int POINT_PER_REPORT = 10;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Test
    void updateBalanceKeepsBalanceConsistentForConcurrentRequests() throws Exception {
        userPointRepository.save(UserPoint.create(USER_ID));
        var readyLatch = new CountDownLatch(THREAD_COUNT);
        var startLatch = new CountDownLatch(1);
        var executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        var futures = new ArrayList<java.util.concurrent.Future<?>>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            long reportId = 100_000L + i;
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                pointService.updateBalance(USER_ID, reportId);
                return null;
            }));
        }

        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        startLatch.countDown();
        for (var future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executorService.shutdown();
        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        UserPoint userPoint = userPointRepository.findByUserId(USER_ID).orElseThrow();
        long ledgerCount = pointLedgerRepository.findAll().stream()
                .filter(pointLedger -> pointLedger.getUserId().equals(USER_ID))
                .map(PointLedger::getIdempotencyKey)
                .count();

        assertThat(userPoint.getBalance()).isEqualTo(THREAD_COUNT * POINT_PER_REPORT);
        assertThat(ledgerCount).isEqualTo(THREAD_COUNT);
    }
}
