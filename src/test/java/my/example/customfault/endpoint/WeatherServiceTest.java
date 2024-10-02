package my.example.customfault.endpoint;

import static my.example.customfault.utils.TestHelper.generateDummyRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.codecentric.namespace.weatherservice.WeatherException;
import de.codecentric.namespace.weatherservice.general.ForecastRequest;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import my.example.customfault.ApplicationTestConfiguration;

@SpringBootTest(classes=ApplicationTestConfiguration.class)
class WeatherServiceTest {

    @Autowired
    private WeatherServiceEndpoint weatherServiceEndpoint;
    
    @Test
    void getCityForecastByZIP() throws WeatherException {
        // Given
        ForecastRequest forecastRequest = generateDummyRequest();
        
        // When
        ForecastReturn forecastReturn = weatherServiceEndpoint.getCityForecastByZIP(forecastRequest);
        
        // Then
        assertNotNull(forecastReturn);
        assertTrue(forecastReturn.isSuccess());
        assertEquals("Weimar", forecastReturn.getCity());
        assertEquals("22%", forecastReturn.getForecastResult().getForecast().get(0).getProbabilityOfPrecipiation().getDaytime());
    }
}
