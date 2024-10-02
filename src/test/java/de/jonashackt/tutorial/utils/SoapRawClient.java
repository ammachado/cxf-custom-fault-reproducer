package de.jonashackt.tutorial.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.jonashackt.tutorial.common.InternalBusinessException;
import de.jonashackt.tutorial.common.XmlUtils;

@Component
public class SoapRawClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(SoapRawClient.class);
	private String soapAction;
	
	private String soapServiceUrl;
	
	public <T> SoapRawClient(String soapServiceUrl, Class<T> jaxWsServiceInterfaceClass) throws InternalBusinessException {
	    this.soapAction = XmlUtils.getSoapActionFromJaxWsServiceInterface(jaxWsServiceInterfaceClass);
	    this.soapServiceUrl = soapServiceUrl;
	}
	
	public SoapRawClientResponse callSoapService(InputStream xmlFile) throws InternalBusinessException {
		SoapRawClientResponse rawSoapResponse;
		
		LOGGER.debug("Calling SoapService with POST on Apache HTTP-Client and configured URL: {}", soapServiceUrl);
		
		try {
			Response httpResponseContainer = Request
					.post(soapServiceUrl)
					.bodyStream(xmlFile, contentTypeTextXmlUtf8())
					.addHeader("SOAPAction", "\"" + soapAction + "\"")
					.execute();

			rawSoapResponse = httpResponseContainer.handleResponse(response -> {
				SoapRawClientResponse soapResponse = new SoapRawClientResponse();
				soapResponse.setHttpStatusCode(response.getCode());
				try {
					soapResponse.setHttpResponseBody(XmlUtils.parseFileStream2Document(response.getEntity().getContent()));
				} catch (Exception ignored) {
				}
				return soapResponse;
			});

		} catch (Exception exception) {
			throw new InternalBusinessException("Some Error occurred while trying to Call SoapService for test: " + exception.getMessage());
		}

		return rawSoapResponse;
	}

	private ContentType contentTypeTextXmlUtf8() {
		return ContentType.create(ContentType.TEXT_XML.getMimeType(), StandardCharsets.UTF_8);
	}
	
}
