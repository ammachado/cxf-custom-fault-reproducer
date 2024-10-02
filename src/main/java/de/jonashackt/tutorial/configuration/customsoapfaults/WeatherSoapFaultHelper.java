package de.jonashackt.tutorial.configuration.customsoapfaults;

import de.codecentric.namespace.weatherservice.exception.WeatherException;
import jakarta.xml.soap.*;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.jonashackt.tutorial.common.FaultConst;
import de.jonashackt.tutorial.common.XmlUtils;
import de.jonashackt.tutorial.logging.SoapFrameworkLogger;
import de.jonashackt.tutorial.transformation.WeatherOutError;

public final class WeatherSoapFaultHelper {

	// private Constructor for Utility-Class
	private WeatherSoapFaultHelper() {
	}

	;

	private static final SoapFrameworkLogger LOG = SoapFrameworkLogger.getLogger(WeatherSoapFaultHelper.class);

	public static void buildWeatherFaultAndSet2SoapMessage(SoapMessage message, FaultConst faultContent) {
		Fault exceptionFault = (Fault) message.getContent(Exception.class);

		//Commenting this and call setCustomSoapBody

//		String originalFaultMessage = exceptionFault.getMessage();
//		exceptionFault.setMessage(faultContent.getMessage());
//		exceptionFault.setDetail(createFaultDetailWithWeatherException(originalFaultMessage, faultContent));
//		message.setContent(Exception.class, exceptionFault);

		setCustomSoapBody(message);  //Cigna Added method to set custom SOAPBody, but nothing was added.



	}

	private static Element createFaultDetailWithWeatherException(String originalFaultMessage, FaultConst faultContent) {
		Element weatherExceptionElementAppended = null;
		try {
			Document weatherExcecption = XmlUtils.marhallJaxbElementIntoDocument(WeatherOutError.createWeatherException(faultContent, originalFaultMessage));
			// As the Root-Element is deleted while adding the WeatherException to the Fault-Details, we have to use a Workaround:
			// we append it to a new Element, which then gets deleted again
			weatherExceptionElementAppended = XmlUtils.appendAsChildElement2NewElement(weatherExcecption);
		} catch (Exception exception) {
			LOG.failedToBuildWeatherServiceCompliantSoapFaultDetails(exception);
			// We don´t want an Exception in the Exceptionhandling
		}
		return weatherExceptionElementAppended;
	}

	/**
	 * This method is called to set custom SOAPBody after SOAPFault is removed.
	 */
	private static void setCustomSoapBody(SoapMessage message) {
		MessageFactory factory = null;
		try {
//			factory = MessageFactory.newInstance();
//			SOAPMessage soapMsg = factory.createMessage();
//			SOAPPart part = soapMsg.getSOAPPart();
//			SOAPEnvelope envelope = part.getEnvelope();
//			SOAPHeader header = envelope.getHeader();
//			SOAPBody body = envelope.getBody();
//			header.addTextNode("Exception");
//			SOAPBodyElement element = body.addBodyElement(envelope.createName("JAVA", "training", "https://balu.com/blog"));
//			element.addChildElement("WS").addTextNode("Training on Web service");
//			SOAPBodyElement element1 = body.addBodyElement(envelope.createName("JAVA", "training", "https://balu.com/blog"));
//			element1.addChildElement("Spring").addTextNode("Training on Spring 3.0");
//			soapMsg.saveChanges();
//			message.setContent(SOAPMessage.class, soapMsg);
			message.removeContent(Exception.class);  //Remove existing fault
			//Setting custom message
            WeatherException exception = new WeatherException();
            message.setContent( WeatherException.class, exception);




		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
