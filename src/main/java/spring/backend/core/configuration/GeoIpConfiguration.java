package spring.backend.core.configuration;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class GeoIpConfiguration {

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        ClassPathResource resource = new ClassPathResource("maxmind/GeoLite2-City.mmdb");
        return new DatabaseReader.Builder(resource.getInputStream())
                .withCache(new CHMCache())
                .build();
    }
}
