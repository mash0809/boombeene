package com.tonem.boombeene.crowdreport.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineUtilsTest {

    @Test
    void distanceMetersCalculatesKnownDistanceBetweenCoordinates() {
        double distance = HaversineUtils.distanceMeters(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616);

        assertThat(distance).isCloseTo(116.0, withinOneMeter());
    }

    @Test
    void isWithinRadiusUsesDefaultOneHundredMetersWhenAccuracyIsZero() {
        boolean withinRadius = HaversineUtils.isWithinRadius(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616,
                0.0);

        assertThat(withinRadius).isFalse();
    }

    @Test
    void isWithinRadiusExpandsRadiusWhenAccuracyExceedsFiftyMeters() {
        boolean withinRadius = HaversineUtils.isWithinRadius(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616,
                100.0);

        assertThat(withinRadius).isTrue();
    }

    private static org.assertj.core.data.Offset<Double> withinOneMeter() {
        return org.assertj.core.data.Offset.offset(1.0);
    }
}
