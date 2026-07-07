package com.tonem.boombeene.point.internal.application;

public interface PointBalanceLockStrategy {

    void earnForCrowdReport(Long userId, Long reportId);
}
