package com.tonem.boombeene.crowdreport.util;

import lombok.experimental.UtilityClass;

/**
 * 두 GPS 좌표 간 지표면 최단 거리를 계산해, 사용자가 제보 대상 가게의 허용 반경 안에 있는지 판단
 * 기본 허용 반경은 50m이며, GPS accuracy가 50m를 초과하면
 * 초과분만큼 반경을 확장해 GPS 오차로 인한 제보 거부를 완화한다.
 */
@UtilityClass
public class HaversineUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final double GPS_ACCURACY_THRESHOLD = 50.0;
    private static final int DEFAULT_ALLOWED_RADIUS = 50;

    // Haversine으로 두 GPS 좌표 사이의 지표면 최단 거리를 계산
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static boolean isWithinAllowedRadius(
            double userLat,
            double userLon,
            double storeLat,
            double storeLon,
            double gpsAccuracy
    ) {
        double allowedRadius = DEFAULT_ALLOWED_RADIUS + Math.max(0.0, gpsAccuracy - GPS_ACCURACY_THRESHOLD);
        return distanceMeters(userLat, userLon, storeLat, storeLon) <= allowedRadius;
    }
}
