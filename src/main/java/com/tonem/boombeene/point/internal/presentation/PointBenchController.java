package com.tonem.boombeene.point.internal.presentation;

import com.tonem.boombeene.point.internal.application.PointEarningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointBenchController {

    private final PointEarningService pointEarningService;

    @PostMapping("/internal/bench/points/earn")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void earn(@Valid @RequestBody PointEarnBenchRequest request) {
        pointEarningService.earnForCrowdReport(request.userId(), request.reportId(), request.lockType());
    }

    public record PointEarnBenchRequest(
            @NotNull Long userId,
            @NotNull Long reportId,
            @NotNull String lockType
    ) {
    }
}
