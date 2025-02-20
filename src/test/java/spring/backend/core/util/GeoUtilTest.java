package spring.backend.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import spring.backend.core.util.geo.GeoUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeoUtilTest {
    @Test
    @DisplayName("서울(37.5665, 126.9780) - 부산(35.1796, 129.0756) 거리 검증")
    void seoulToBusan() {
        // Given
        double seoulLat = 37.5665;
        double seoulLon = 126.9780;
        double busanLat = 35.1796;
        double busanLon = 129.0756;

        // When
        double distance = GeoUtil.calculateDistanceBetweenTwoCoordinate(
                seoulLat, seoulLon,
                busanLat, busanLon
        );

        // Then (실제 거리: 약 325km)
        assertEquals(325.0, distance, 10.0);
    }

    @Test
    @DisplayName("서울(37.5665, 126.9780) - 인천(37.4500, 126.7000) 거리 검증")
    void seoulToIncheon() {
        double distance = GeoUtil.calculateDistanceBetweenTwoCoordinate(
                37.5665, 126.9780,
                37.4500, 126.7000
        );
        assertEquals(28.0, distance, 2.0);
    }

    @Test
    @DisplayName("동일 좌표 거리 계산")
    void sameCoordinates() {
        double distance = GeoUtil.calculateDistanceBetweenTwoCoordinate(
                37.5665, 126.9780,
                37.5665, 126.9780
        );
        assertEquals(0.0, distance, 0.0);
    }

}
