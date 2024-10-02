package my.example.customfault;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import de.codecentric.namespace.weatherservice.WeatherService;
import my.example.customfault.configuration.ApplicationConfiguration;
import my.example.customfault.endpoint.WeatherServiceEndpoint;
import org.springframework.context.annotation.Primary;

@Configuration
@Import(ApplicationConfiguration.class)
public class ApplicationTestConfiguration {

    @Bean
    @Primary
    public WeatherService weatherService() {
        return new WeatherServiceEndpoint();
    }
}
