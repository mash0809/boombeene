package com.tonem.boombeene.crowdreport.util;

import org.assertj.core.data.Offset;
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
    void isWithinRadiusUsesDefaultFiftyMetersWhenAccuracyIsZero() {
        boolean withinRadius = HaversineUtils.isWithinAllowedRadius(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616,
                0.0);

        assertThat(withinRadius).isFalse();
    }

    @Test
    void isWithinRadiusExpandsRadiusWhenAccuracyExceedsFiftyMeters() {
        boolean withinRadius = HaversineUtils.isWithinAllowedRadius(
                37.5662952, 126.9779451,
                37.5657037, 126.9768616,
                120.0);

        assertThat(withinRadius).isTrue();
    }

    private static Offset<Double> withinOneMeter() {
        return Offset.offset(1.0);
    }
}
