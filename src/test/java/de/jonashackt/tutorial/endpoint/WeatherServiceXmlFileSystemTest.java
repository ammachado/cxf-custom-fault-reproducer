package de.jonashackt.tutorial.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codecentric.namespace.weatherservice.WeatherException;
import de.codecentric.namespace.weatherservice.WeatherService;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import de.codecentric.namespace.weatherservice.general.GetCityForecastByZIP;
import de.jonashackt.tutorial.SimpleBootCxfSystemTestApplication;
import de.jonashackt.tutorial.common.XmlUtils;
import de.jonashackt.tutorial.common.InternalBusinessException;


@SpringBootTest(classes=SimpleBootCxfSystemTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@WebIntegrationTest("server.port:8090")
public class WeatherServiceXmlFileSystemTest {

    @Autowired
    private WeatherService weatherServiceSystemTestClient;
    
    @Value(value="classpath:requests/GetCityForecastByZIPTest.xml")
    private Resource getCityForecastByZIPTestXml;
        
    @Test
    public void getCityForecastByZIP() throws WeatherException, InternalBusinessException, IOException {
        // Given
        GetCityForecastByZIP getCityForecastByZIP = XmlUtils.readSoapMessageFromStreamAndUnmarshallBody2Object(getCityForecastByZIPTestXml.getInputStream(), GetCityForecastByZIP.class);
        
        // When
        ForecastReturn forecastReturn = weatherServiceSystemTestClient.getCityForecastByZIP(getCityForecastByZIP.getForecastRequest());
        
        // Then
        assertNotNull(forecastReturn);
        assertEquals(true, forecastReturn.isSuccess());
        assertEquals("Weimar", forecastReturn.getCity());
        assertEquals("22%", forecastReturn.getForecastResult().getForecast().get(0).getProbabilityOfPrecipiation().getDaytime());
    }
}
