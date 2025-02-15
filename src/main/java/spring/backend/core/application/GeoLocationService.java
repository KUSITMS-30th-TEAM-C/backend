package spring.backend.core.application;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import spring.backend.core.util.geo.dto.response.Coordinate;

import java.io.IOException;
import java.net.InetAddress;

import static spring.backend.core.util.geo.GeoUtil.calculateDistanceBetweenTwoCoordinate;

@Service
@RequiredArgsConstructor
@Log4j2
public class GeoLocationService {
    private final DatabaseReader reader;

    public boolean checkUserLocation(String newIp, String savedIp) throws IOException, GeoIp2Exception {
        Coordinate newIpCoordinate = getCoordinate(newIp);
        Coordinate savedIpCoordinate = getCoordinate(savedIp);

        double distanceBetweenIp = calculateDistanceBetweenTwoCoordinate(
                savedIpCoordinate.latitude(),
                savedIpCoordinate.longitude(),
                newIpCoordinate.latitude(),
                newIpCoordinate.longitude()
        );

        return distanceBetweenIp > 100;
    }

    private Coordinate getCoordinate(String ip) throws GeoIp2Exception, IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        CityResponse response = reader.city(ipAddress);
        Location location = response.getLocation();
        return Coordinate.of(
                location.getLatitude(),
                location.getLongitude()
        );
    }
}
