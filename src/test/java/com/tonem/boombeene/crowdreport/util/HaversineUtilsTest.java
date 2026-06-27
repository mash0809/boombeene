package com.tonem.boombeene.crowdreport.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineUtilsTest {

    // (37.5662952, 126.9779451) ~ (37.5657037, 126.9768616) 간 거리: 약 116m
    @Test
    void distanceMetersCalculatesKnownDistanceBetweenCoordinates() {
        double distance = HaversineUtils.distanceMeters(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616);

        assertThat(distance).isCloseTo(116.0, Offset.offset(1.0));
    }

    @Test
    void isWithinRadiusUsesDefaultFiftyMetersWhenAccuracyIsZero() {
        // 허용 반경: 50m이므로 false
        boolean withinRadius = HaversineUtils.isWithinAllowedRadius(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616,
                0.0);

        assertThat(withinRadius).isFalse();
    }

    @Test
    void isWithinRadiusExpandsRadiusWhenAccuracyExceedsFiftyMeters() {
        // 허용 반경: 50m + (GPS accuracy - 50m)이므로, accuracy 120m일 때 최종 허용 반경은 120m으로 true
        boolean withinRadius = HaversineUtils.isWithinAllowedRadius(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616,
                120.0);

        assertThat(withinRadius).isTrue();
    }
}
