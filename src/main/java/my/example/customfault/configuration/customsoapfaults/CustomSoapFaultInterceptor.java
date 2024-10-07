package my.example.customfault.configuration.customsoapfaults;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
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

@Slf4j
public class CustomSoapFaultInterceptor extends AbstractSoapInterceptor {

    private static final SoapFrameworkLogger LOG = SoapFrameworkLogger.getLogger(CustomSoapFaultInterceptor.class);

    public CustomSoapFaultInterceptor() {
        super(Phase.PRE_STREAM);
    }

    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {

        log.debug("Running Custom Interceptor ...");
        // Fault message
        final Fault soapFault = (Fault) soapMessage.getContent(Exception.class);
        final Throwable faultCause = soapFault.getCause();
        final String faultMessage = soapFault.getMessage();

        // Whether to customize SOAP response
        boolean whetherToSwapSOAPMessage = false;
        if (containsFaultIndicatingNotSchemeCompliantXml(faultCause, faultMessage)) {
            LOG.schemaValidationError(FaultConst.SCHEME_VALIDATION_ERROR, faultMessage);
            whetherToSwapSOAPMessage = true;
//            WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage, FaultConst.SCHEME_VALIDATION_ERROR);
        } else if (isExpectedFaultCauseType(faultCause)) {
            LOG.schemaValidationError(FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR, faultMessage);
            whetherToSwapSOAPMessage = true;
//            WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage, FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
        }

        if (whetherToSwapSOAPMessage) {
            log.debug("Fault cause {}", faultMessage);

            // SOAP exchange (containing both in- and out- messages)
            final Exchange exchange = soapMessage.getExchange();

            // Clear out the current output stream (by replacing it with a fresh OS)
            CachedOutputStream cachedOutputStream = new CachedOutputStream();
            soapMessage.setContent(OutputStream.class, cachedOutputStream);
            // Run the interceptor on the current message
            soapMessage.getInterceptorChain().doIntercept(soapMessage);
            try {
                cachedOutputStream.flush();

                // Obtain the cached output stream for enrichment
                cachedOutputStream = (CachedOutputStream) soapMessage.getContent(OutputStream.class);

                // Obtain the custom SOAP message for the given fault
                final String soapMessageXml = SoapUtils.replaceFaultNodeAndGetSoapString(cachedOutputStream.getInputStream());

                // Ensure the outgoing message exists, if not use the fault message
                Message outMessage = Optional.ofNullable(exchange.getOutMessage())
                        .orElse(exchange.getOutFaultMessage());
                exchange.setOutMessage(outMessage);

                // Clear the fault message
                exchange.setOutFaultMessage(null);

                // Update HTTP response status code
                outMessage.put(Message.RESPONSE_CODE, 200);

                // Obtain the underlying HTTP response
                HttpServletResponse response = (HttpServletResponse) outMessage
                        .get(AbstractHTTPDestination.HTTP_RESPONSE);
                // SOAP content type
                response.setContentType("text/xml");

                // Write the SOAP XML to the HTTP response
                final PrintWriter writer = response.getWriter();
                writer.write(soapMessageXml);
                outMessage.put(AbstractHTTPDestination.HTTP_RESPONSE, response);
                writer.close();
            } catch (Exception ex) {
                log.error("Error while building outgoing HTTP response", ex);
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

    /**
     * Implementation for support of caching an output stream temporarily.
     */
    private static class CachedStream extends CachedOutputStream {
        public CachedStream() {
            super();
        }

        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        protected void doClose() throws IOException {
        }

        protected void onWrite() throws IOException {
        }
    }
}
