package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.crowdreport.CrowdReportCompleted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    /**
     * updateBalance 는 분산 락이 적용된 메서드로 만약 listener 레벨에서 트랜잭션이 열리면 락이 트랜잭션 커밋보다 먼저 unlock 될 수 있다.
     * 따라서 리스너는 트랜잭션 없이 실행하고, updateBalance 트랜잭션이 분산 락 안에서 직접 시작/커밋되게 한다.
     * 단 Spring Modulith 에서 리스너에 @ApplicationModuleListener 을 쓰는 것을 권장하므로, @Async & @TransactionalEventListener 를 따로 사용하기 보단
     * @ApplicationModuleListener 을 그대로 사용하면서 propagation 을 NOT_SUPPORTED 로 설정한다. {@link ApplicationModuleListener}
     */
    @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
    public void onCrowdReport(CrowdReportCompleted event) {
        pointService.updateBalance(event.userId(), event.reportId());
    }
}
