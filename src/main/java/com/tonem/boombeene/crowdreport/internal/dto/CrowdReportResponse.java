package com.tonem.boombeene.crowdreport.internal.dto;

public record CrowdReportResponse(Long reportId) {

    public static CrowdReportResponse from(CrowdReportDto report) {
        return new CrowdReportResponse(report.reportId());
    }
}
