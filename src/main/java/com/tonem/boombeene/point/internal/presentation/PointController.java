package com.tonem.boombeene.point.internal.presentation;

import com.tonem.boombeene.point.internal.application.PointService;
import com.tonem.boombeene.point.internal.dto.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/balance")
    public PointResponse getBalance(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return new PointResponse(pointService.getBalance(userId));
    }
}
