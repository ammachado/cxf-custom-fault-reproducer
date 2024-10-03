package my.example.customfault.endpoint;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.codecentric.namespace.weatherservice.exception.WeatherException;
import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.SimpleBootCxfSystemTestApplication;
import my.example.customfault.common.FaultConst;
import my.example.customfault.common.XmlUtils;
import my.example.customfault.utils.SoapRawClient;
import my.example.customfault.utils.SoapRawClientResponse;


@SpringBootTest(classes=SimpleBootCxfSystemTestApplication.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
class WeatherServiceXmlErrorSystemTest {

	@Autowired
	private SoapRawClient soapRawClient;

	@Value(value="classpath:requests/xmlerrors/xmlErrorNotXmlSchemeCompliantUnderRootElementTest.xml")
	private Resource xmlErrorNotXmlSchemeCompliantUnderRootElementTestXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorNotXmlSchemeCompliantRootElementTest.xml")
	private Resource xmlErrorNotXmlSchemeCompliantRootElementTestXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorSoapHeaderMissingSlash.xml")
	private Resource xmlErrorSoapHeaderMissingSlashXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorSoapBodyTagMissingBracketTest.xml")
	private Resource xmlErrorSoapBodyTagMissingBracketTestXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorSoapHeaderTagMissingBracketTest.xml")
	private Resource xmlErrorSoapHeaderTagMissingBracketTestXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorSoapEnvelopeTagMissingBracketTest.xml")
	private Resource xmlErrorSoapEnvelopeTagMissingBracketTestXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorXMLHeaderDefinitionMissingBracket.xml")
	private Resource xmlErrorXMLHeaderDefinitionMissingBracketXml;
	
	@Value(value="classpath:requests/xmlerrors/xmlErrorXMLTagNotClosedInsideBodyTest.xml")
	private Resource xmlErrorXMLTagNotClosedInsideBodyTestXml;


	/*
	 * Non-Scheme-compliant Errors
	 */
	
	@Test
	void xmlErrorNotXmlSchemeCompliantUnderRootElementTest() throws Exception {
        log.warn("Test Resource {} ", xmlErrorNotXmlSchemeCompliantUnderRootElementTestXml);
		checkXmlError(xmlErrorNotXmlSchemeCompliantUnderRootElementTestXml, FaultConst.SCHEME_VALIDATION_ERROR);
	}
	
	@Test
	void xmlErrorNotXmlSchemeCompliantRootElementTest() throws Exception {
		checkXmlError(xmlErrorNotXmlSchemeCompliantRootElementTestXml, FaultConst.SCHEME_VALIDATION_ERROR);
	}
	
	@Test
	void xmlErrorSoapHeaderMissingSlash() throws Exception {
		checkXmlError(xmlErrorSoapHeaderMissingSlashXml, FaultConst.SCHEME_VALIDATION_ERROR);
	}	
	
	/*
	 * Errors with syntactically incorrect XML
	 */
	
	@Test
	void xmlErrorSoapBodyTagMissingBracketTest() throws Exception {
		checkXmlError(xmlErrorSoapBodyTagMissingBracketTestXml, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
	}
	
	@Test
	void xmlErrorSoapHeaderTagMissingBracketTest() throws Exception {
		checkXmlError(xmlErrorSoapHeaderTagMissingBracketTestXml, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
	}
	
	@Test
	void xmlErrorSoapEnvelopeTagMissingBracketTest() throws Exception {
		checkXmlError(xmlErrorSoapEnvelopeTagMissingBracketTestXml, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
	}
	
	@Test
	void xmlErrorXMLHeaderDefinitionMissingBracket() throws Exception {
		checkXmlError(xmlErrorXMLHeaderDefinitionMissingBracketXml, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
	}	
	
	@Test
	void xmlErrorXMLTagNotClosedInsideBodyTest() throws Exception {
		checkXmlError(xmlErrorXMLTagNotClosedInsideBodyTestXml, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
	}

	private void checkXmlError(Resource testFile, FaultConst faultContent) throws Exception {
		// When
		log.warn("Test file: {}", testFile);
		SoapRawClientResponse soapRawResponse = soapRawClient.callSoapService(testFile.getInputStream());

		// Then
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			softly.assertThat(soapRawResponse)
					.isNotNull()
					.hasFieldOrPropertyWithValue("httpStatusCode", 500).describedAs("200 OK response expected")
					.hasFieldOrPropertyWithValue("faultStringValue", faultContent.getMessage())
					;

			Document doc = soapRawResponse.getHttpResponseBody();
			NodeList elementsByTagName = doc.getElementsByTagName("detail");
			assertNotNull(elementsByTagName);
			Node details = elementsByTagName.item(0).getFirstChild();
			
			JAXBElement<WeatherException> weatherJaxb = XmlUtils.unmarshallNode(details, WeatherException.class);
			assertNotNull(weatherJaxb);
			WeatherException weatherException = unmarshallNode.getValue();
			assertNotNull(weatherException);
			assertEquals("ExtremeRandomNumber", weatherException.getUuid());
		}
	}
	
	
	
}
