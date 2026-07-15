package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.AbstractIntegrationTest;
import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.PointLedgerRepository;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
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
    @DisplayName("동시 요청이 발생해도 잔액 정합성을 유지한다")
    void updateBalanceKeepsBalanceConsistentForConcurrentRequests() throws Exception {
        // 신규 row 생성 경쟁이 아니라 기존 balance 갱신의 lost update 여부만 검증한다.
        userPointRepository.save(UserPoint.create(USER_ID));

        var readyLatch = new CountDownLatch(THREAD_COUNT);
        var startLatch = new CountDownLatch(1);
        var executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        var futures = new ArrayList<Future<?>>();

        // 모든 작업 스레드를 먼저 대기시킨 뒤 한 번에 출발시켜 동일 유저 갱신을 최대한 겹치게 만든다.
        for (int i = 0; i < THREAD_COUNT; i++) {
            long reportId = 100_000L + i;
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                pointService.updateBalance(USER_ID, reportId);
                return null;
            }));
        }

        // 메인 스레드가 readyLatch 의 count 가 0이 될 때까지 대기
        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        // 모든 포인트 적립 로직을 실행
        startLatch.countDown();

        // Future 결과를 모두 확인
        for (var future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }
        executorService.shutdown();

        UserPoint userPoint = userPointRepository.findByUserId(USER_ID).orElseThrow();
        long ledgerCount = pointLedgerRepository.findAll().stream()
                .filter(pointLedger -> pointLedger.getUserId().equals(USER_ID))
                .count();

        // 모든 point 적립이 누락 없이 balance 에 적립되고, ledger 도 스레드 개수만큼 생성
        assertThat(userPoint.getBalance()).isEqualTo(THREAD_COUNT * POINT_PER_REPORT);
        assertThat(ledgerCount).isEqualTo(THREAD_COUNT);
    }
}
