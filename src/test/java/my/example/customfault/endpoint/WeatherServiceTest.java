package my.example.customfault.endpoint;

import static java.nio.charset.StandardCharsets.UTF_8;
import static my.example.customfault.configuration.WebServiceConfiguration.BASE_URL;
import static my.example.customfault.configuration.WebServiceConfiguration.SERVICE_URL;
import static my.example.customfault.utils.TestHelper.generateDummyRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

import de.codecentric.namespace.weatherservice.WeatherException;
import de.codecentric.namespace.weatherservice.general.ForecastRequest;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.SimpleBootCxfSystemTestApplication;
import my.example.customfault.common.XmlUtils;

@SpringBootTest(classes= SimpleBootCxfSystemTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
class WeatherServiceTest {
    String HOST = "http://localhost:8080";
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
        assertEquals("22%", forecastReturn.getForecastResult().getForecasts().get(0).getProbabilityOfPrecipiation().getDaytime());
    }


    @Test
    public void testMarshallingError() throws Exception {

        String SOAP_SERVICE_URL = HOST + BASE_URL + SERVICE_URL;
        log.warn("URL invoked: {}", SOAP_SERVICE_URL);
        //log.warn("Local port {}", port);
        //log.warn("management port {} ", managementPort);
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("requests/xmlerrors/xmlErrorNotXmlSchemeCompliantRootElementTest.xml");
        //Thread.sleep(25000);
        Response httpResponseContainer = Request
                .post(SOAP_SERVICE_URL)
                .addHeader("SOAPAction","http://www.codecentric.de/namespace/weatherservice/GetCityForecastByZIP")
                .bodyStream(xmlStream, contentTypeTextXmlUtf8())
                .execute();
        HttpResponse httpResponse = httpResponseContainer.returnResponse();
        log.warn("Http response {}", httpResponse.getCode());
        log.warn("Content length {} ", ((BasicClassicHttpResponse) httpResponse).getEntity().getContentLength());
        assertTrue(((BasicClassicHttpResponse) httpResponse).getEntity().getContentLength() > 0);
        if (((BasicClassicHttpResponse) httpResponse).getEntity().getContentLength() > 0) {
            Document document = XmlUtils.parseFileStream2Document(((BasicClassicHttpResponse) httpResponse).getEntity().getContent());
            assertNull(document.getElementsByTagName("soap:Fault").item(0));


        }


    }


    private ContentType contentTypeTextXmlUtf8() {
        return ContentType.create(ContentType.TEXT_XML.getMimeType(), UTF_8);
    }

}
