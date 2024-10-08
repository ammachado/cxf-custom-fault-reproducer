package my.example.customfault.configuration.customsoapfaults;

import de.codecentric.namespace.weatherservice.exception.WeatherException;
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

    /**
     * Build a SOAP document node with custom fault information
     *
     * @param soapMessage  SOAP message with fault
     * @param faultContent Custom fault message mappings
     */
    public static Element buildWeatherFaultAndSet2SoapMessage(SoapMessage soapMessage, FaultConst faultContent) {
        final Fault soapFault = (Fault) soapMessage.getContent(Exception.class);
        final String faultMessage = soapFault.getMessage();
        final Element faultDetailElement = createFaultDetailWithWeatherException(faultMessage, faultContent);
        soapMessage.setContent(Element.class, faultDetailElement);
        return faultDetailElement;
    }

    /**
     * Create a new weather exception instance using the given fault message and customization
     *
     * @param faultMessage SOAP fault message
     * @param faultContent Custom fault mappings
     * @return Root element of the weather exception
     */
    private static Element createFaultDetailWithWeatherException(String faultMessage, FaultConst faultContent) {
        Element parentElement = null;
        try {
            final WeatherException weatherException = WeatherOutError.createWeatherException(faultContent, faultMessage);
            final Document document = XmlUtils.marshallJaxbElementIntoDocument(weatherException);
            parentElement = XmlUtils.appendAsChildElement2NewElement(document);
        } catch (Exception exception) {
            LOG.failedToBuildWeatherServiceCompliantSoapFaultDetails(exception);
        }
        return parentElement;
    }
}