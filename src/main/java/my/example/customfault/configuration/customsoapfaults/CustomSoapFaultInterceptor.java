package my.example.customfault.configuration.customsoapfaults;

import java.io.*;
import java.util.Objects;
import java.util.Optional;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.ctc.wstx.exc.WstxException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.UnmarshalException;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.common.FaultConst;
import my.example.customfault.common.SoapUtils;
import my.example.customfault.logging.SoapFrameworkLogger;
import org.w3c.dom.Element;

@Slf4j
public class CustomSoapFaultInterceptor extends AbstractSoapInterceptor {

    private static final SoapFrameworkLogger LOG = SoapFrameworkLogger.getLogger(CustomSoapFaultInterceptor.class);

    public CustomSoapFaultInterceptor() {
        super(Phase.PRE_STREAM);
        addBefore(SoapPreProtocolOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        // Only interested in outbound messages
        if (!MessageUtils.isOutbound(soapMessage)) {
            return;
        }

        // Only interested in SOAP faults
        final Exception exception = soapMessage.getContent(Exception.class);
        if (exception instanceof Fault soapFault) {
            // Fault message
            final Throwable faultCause = soapFault.getCause();
            final String faultMessage = soapFault.getMessage();

            // Whether to customize SOAP response
            FaultConst faultConst = null;
            if (containsFaultIndicatingNotSchemeCompliantXml(faultCause, faultMessage)) {
                faultConst = FaultConst.SCHEME_VALIDATION_ERROR;
            } else if (isExpectedFaultCauseType(faultCause)) {
                faultConst = FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR;
            }

            if (Objects.nonNull(faultConst)) {
                log.debug("SOAP Fault message = {}", faultMessage);
                LOG.schemaValidationError(faultConst, faultMessage);

                // Clear out the current output stream (by replacing it with a fresh OS)
                CachedOutputStream cachedOutputStream = new CachedOutputStream();
                soapMessage.setContent(OutputStream.class, cachedOutputStream);
                // Run the interceptor on the current message
                soapMessage.getInterceptorChain().doIntercept(soapMessage);

                try {
                    cachedOutputStream.flush();

                    // Obtain the cached output stream for enrichment
                    cachedOutputStream = (CachedOutputStream) soapMessage.getContent(OutputStream.class);

                    // Customized exception
                    final Element weatherExceptionElement = WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage, faultConst);

                    // Obtain the custom SOAP XML for the given exception, built from the SOAP message stream
                    final String soapMessageXml = SoapUtils.replaceFaultNodeAndGetSoapString(cachedOutputStream.getInputStream(), weatherExceptionElement);

                    // SOAP exchange
                    final Exchange exchange = soapMessage.getExchange();

                    // Ensure the outgoing message exists, if not use the fault message
                    final Message outMessage = Optional.ofNullable(exchange.getOutMessage()).orElse(exchange.getOutFaultMessage());

                    // Clear the fault message
                    exchange.setOutFaultMessage(null);
                    exchange.setOutMessage(outMessage);

                    // Update HTTP response status code
                    outMessage.put(Message.RESPONSE_CODE, 200);

                    // Obtain the underlying HTTP response
                    HttpServletResponse httpResponse = (HttpServletResponse) outMessage.get(AbstractHTTPDestination.HTTP_RESPONSE);
                    httpResponse.setContentType("text/xml");
                    httpResponse.setCharacterEncoding("UTF-8");
                    httpResponse.setStatus(200);

                    // Write the SOAP XML to the HTTP response
                    final PrintWriter httpPrintWriter = httpResponse.getWriter();
                    httpPrintWriter.write(soapMessageXml);
                    outMessage.put(AbstractHTTPDestination.HTTP_RESPONSE, httpResponse);
                    httpPrintWriter.close();
                } catch (Exception ex) {
                    log.error("Error building outgoing customized SOAP response", ex);
                }
            }
        }
    }

    /**
     * Whether the fault contains message indicating that XML is not schema-compliant
     *
     * @param faultCause   SOAP fault
     * @param faultMessage Fault message
     * @return <code>true</code> if the fault message contains <b>"Unexpected wrapper element"</b>
     */
    private boolean containsFaultIndicatingNotSchemeCompliantXml(Throwable faultCause, String faultMessage) {
        return (faultCause instanceof UnmarshalException
                // 1.) If the root-Element of the SoapBody is syntactically correct, but not
                // scheme-compliant,
                // there is no UnmarshalException and we have to look for
                // 2.) Missing / lead to Faults without Causes, but to Messages like "Unexpected
                // wrapper element XYZ found. Expected"
                // One could argue, that this is syntactically incorrect, but here we just take
                // it as Non-Scheme-compliant
                || Objects.nonNull(faultMessage) && faultMessage.contains("Unexpected wrapper element"));
    }

    /**
     * Whether the fault cause is an instance of <code>WstxException</code>, <code>WstxUnexpectedCharException</code>,
     * or <code>IllegalArgumentException</code>.
     *
     * @param faultCause Fault cause
     * @return <code>true</code> if the fault or its cause match the exception types listed, otherwise <code>false</code>
     */
    private boolean isExpectedFaultCauseType(Throwable faultCause) {
        return (faultCause instanceof WstxException
                // If Xml-Header is invalid, there is a wrapped Cause in the original Cause we have to check
                || Objects.nonNull(faultCause) && faultCause.getCause() instanceof WstxUnexpectedCharException
                || faultCause instanceof IllegalArgumentException);
    }
}
