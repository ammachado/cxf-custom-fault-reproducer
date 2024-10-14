package my.example.customfault.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import de.codecentric.namespace.weatherservice.WeatherException;
import de.codecentric.namespace.weatherservice.WeatherService;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import de.codecentric.namespace.weatherservice.general.GetCityForecastByZIP;
import my.example.customfault.SimpleBootCxfSystemTestApplication;
import my.example.customfault.common.InternalBusinessException;
import my.example.customfault.common.XmlUtils;

@SpringBootTest(classes=SimpleBootCxfSystemTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WeatherServiceXmlFileSystemTest {

    @Autowired
    private WeatherService weatherServiceSystemTestClient;
    
    @Value(value="classpath:requests/GetCityForecastByZIPTest.xml")
    private Resource getCityForecastByZIPTestXml;
        
    @Test
    void getCityForecastByZIP() throws WeatherException, InternalBusinessException, IOException {
        // Given
        GetCityForecastByZIP getCityForecastByZIP = XmlUtils.readSoapMessageFromStreamAndUnmarshallBody2Object(getCityForecastByZIPTestXml.getInputStream(), GetCityForecastByZIP.class);
        
        // When
        ForecastReturn forecastReturn = weatherServiceSystemTestClient.getCityForecastByZIP(getCityForecastByZIP.getForecastRequest());
        
        // Then
        assertNotNull(forecastReturn);
        assertTrue(forecastReturn.isSuccess());
        assertEquals("Weimar", forecastReturn.getCity());
        assertEquals("22%", forecastReturn.getForecastResult().getForecast().get(0).getProbabilityOfPrecipiation().getDaytime());
    }
}
