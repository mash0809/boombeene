package com.tonem.boombeene.crowdreport.dto;

public record CrowdReportResponse(Long reportId) {

    public static CrowdReportResponse from(CrowdReportDto report) {
        return new CrowdReportResponse(report.reportId());
    }
}
