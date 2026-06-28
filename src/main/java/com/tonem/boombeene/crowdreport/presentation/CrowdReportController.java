package com.tonem.boombeene.crowdreport.presentation;

import com.tonem.boombeene.crowdreport.application.CrowdReportService;
import com.tonem.boombeene.crowdreport.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.dto.CrowdReportResponse;
import com.tonem.boombeene.crowdreport.dto.StoreCongestionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crowd-reports")
@RequiredArgsConstructor
public class CrowdReportController {

    private final CrowdReportService crowdReportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrowdReportResponse report(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody CrowdReportRequest request) {
        return CrowdReportResponse.from(crowdReportService.report(userId, request));
    }

    @GetMapping("/stores/{storeId}/congestion")
    public StoreCongestionResponse getCongestion(
            @PathVariable Long storeId,
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        return StoreCongestionResponse.from(storeId, crowdReportService.getCongestion(storeId, latitude, longitude));
    }
}
