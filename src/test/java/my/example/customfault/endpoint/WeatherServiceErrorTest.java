package my.example.customfault.endpoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.xml.transform.Source;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.xml.transform.StringSource;

import de.codecentric.namespace.weatherservice.datatypes.InvocationOutcomeType;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import de.codecentric.namespace.weatherservice.general.GetCityForecastByZIPResponse;
import jakarta.xml.bind.JAXB;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.SimpleBootCxfSystemTestApplication;
import my.example.customfault.common.FaultConst;
import my.example.customfault.utils.SoapRawClient;
import my.example.customfault.utils.SoapRawClientResponse;

@Slf4j
@SpringBootTest(classes = SimpleBootCxfSystemTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WeatherServiceErrorTest {

	private final String CITYFORECAST_SOAPACTION ="http://www.codecentric.de/namespace/weatherservice/GetCityForecastByZIP";
	
	private final String WEATHERINFO_SOAPACTION ="http://www.codecentric.de/namespace/weatherservice/GetWeatherInformation";
	
	@Autowired
	private SoapRawClient soapRawClient;

	@Value(value = "classpath:requests/weather/schemaErrorInnerElementWrong.xml")
	private Resource schemaErrorInnerElementWrong;

	@Value(value = "classpath:requests/weather/schemaErrorTypeWrong.xml")
	private Resource schemaErrorTypeWrong;

	@Value(value = "classpath:requests/weather/xmlTagNotClosedForRoot.xml")
	private Resource xmlTagNotClosedForRoot;

	@Value(value = "classpath:requests/weather/xmlTagNotClosedForInnerObject.xml")
	private Resource xmlTagNotClosedForInnerObject;
	
	@Value(value = "classpath:requests/weather/wrongElementInBody.xml")
	private Resource wrongElementInBody;
	
	@Value(value = "classpath:requests/weather/headerNotClosed.xml")
	private Resource headerNotClosed;
	
	@Value(value = "classpath:requests/weather/testResponseElementWrapper.xml")
	private Resource testResponseElementWrapper;
	
	@Test
	void testSchemaErrorInnerElementWrong() throws Exception {
		log.warn("Test Resource {} ", schemaErrorInnerElementWrong);
		checkXmlError(schemaErrorInnerElementWrong, FaultConst.SCHEME_VALIDATION_ERROR,CITYFORECAST_SOAPACTION);
	}

	@Test
	void testSchemaErrorTypeWrong() throws Exception {
		log.warn("Test Resource {} ", schemaErrorTypeWrong);
		checkXmlError(schemaErrorTypeWrong, FaultConst.SCHEME_VALIDATION_ERROR,CITYFORECAST_SOAPACTION);
	}

	@Test
	void testxmlTagNotClosedForRoot() throws Exception {
		log.warn("Test Resource {} ", xmlTagNotClosedForRoot);
		checkXmlError(xmlTagNotClosedForRoot, FaultConst.SCHEME_VALIDATION_ERROR,CITYFORECAST_SOAPACTION);
	}
	
	@Test
	void testxmlTagNotClosedForInnerObject() throws Exception {
		log.warn("Test Resource {} ", xmlTagNotClosedForInnerObject);
		checkXmlError(xmlTagNotClosedForInnerObject, FaultConst.SCHEME_VALIDATION_ERROR,CITYFORECAST_SOAPACTION);
	}
	
	@Test
	void testWrongElementInBody() throws Exception {
		log.warn("Test Resource {} ", wrongElementInBody);
		checkXmlError(wrongElementInBody, FaultConst.SCHEME_VALIDATION_ERROR,CITYFORECAST_SOAPACTION);
	}
	
	void testWithDifferentOperationForCorrectResponse() throws Exception{
		log.warn("Test Resource {} ", testResponseElementWrapper);
		checkXmlError(testResponseElementWrapper, FaultConst.SCHEME_VALIDATION_ERROR,WEATHERINFO_SOAPACTION);
	}

	private void checkXmlError(Resource testFile, FaultConst faultContent,String soapAction) throws Exception {
		// When
		log.warn("Test file: {}", testFile);
		SoapRawClientResponse soapRawResponse = soapRawClient.callSoapService(testFile.getInputStream(),
				soapAction);
		// Then
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			softly.assertThat(soapRawResponse).isNotNull().hasFieldOrPropertyWithValue("httpStatusCode", 200)
					.describedAs("200 OK response expected")
			// .hasFieldOrPropertyWithValue("faultStringValue", null)
			;

			String bodyStringValue = soapRawResponse.getBodyStringValue();

			String result = new BufferedReader(new InputStreamReader(testFile.getInputStream())).lines()
					.collect(Collectors.joining("\n"));
			log.warn("{}", result);
			log.warn("{}", bodyStringValue);
			Source stringSource = new StringSource(soapRawResponse.getBodyStringValue());

			GetCityForecastByZIPResponse unmarshalled = JAXB.unmarshal(stringSource,
					GetCityForecastByZIPResponse.class);
			ForecastReturn getCityForecastByZIPResult = unmarshalled.getGetCityForecastByZIPResult();
			softly.assertThat(getCityForecastByZIPResult).isNotNull().hasFieldOrProperty("invocationOutcome")
					.extracting(ForecastReturn::getInvocationOutcome,
							InstanceOfAssertFactories.type(InvocationOutcomeType.class))
					.hasFieldOrPropertyWithValue("message", faultContent.getMessage());
		}
	}
}
