package com.tonem.boombeene.crowdreport.util;

/**
 * 두 GPS 좌표 간 지표면 최단 거리를 계산해, 사용자가 제보 대상 가게의
 * 허용 반경 안에 있는지 판단한다.
 *
 * 기본 허용 반경은 100m이며, GPS accuracy가 50m를 초과하면
 * 초과분만큼 반경을 확장해 GPS 오차로 인한 제보 거부를 완화한다.
 */
public final class HaversineUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private HaversineUtils() {
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static boolean isWithinRadius(double userLat, double userLon,
                                         double storeLat, double storeLon,
                                         double gpsAccuracy) {
        double allowedRadius = 100.0 + Math.max(0.0, gpsAccuracy - 50.0);
        return distanceMeters(userLat, userLon, storeLat, storeLon) <= allowedRadius;
    }
}
