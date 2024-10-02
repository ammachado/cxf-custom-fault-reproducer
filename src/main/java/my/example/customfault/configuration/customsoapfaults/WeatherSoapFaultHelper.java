package my.example.customfault.configuration.customsoapfaults;

import my.example.customfault.common.FaultConst;
import my.example.customfault.common.XmlUtils;
import my.example.customfault.logging.SoapFrameworkLogger;
import my.example.customfault.transformation.WeatherOutError;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class WeatherSoapFaultHelper {

	// private Constructor for Utility-Class
	private WeatherSoapFaultHelper() {
	}

	private static final SoapFrameworkLogger LOG = SoapFrameworkLogger.getLogger(WeatherSoapFaultHelper.class);

	public static void buildWeatherFaultAndSet2SoapMessage(SoapMessage message, FaultConst faultContent) {
		Fault exceptionFault = (Fault) message.getContent(Exception.class);

		String originalFaultMessage = exceptionFault.getMessage();
		exceptionFault.setMessage(faultContent.getMessage());
		exceptionFault.setDetail(createFaultDetailWithWeatherException(originalFaultMessage, faultContent));
		message.setContent(Exception.class, exceptionFault);
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
}
