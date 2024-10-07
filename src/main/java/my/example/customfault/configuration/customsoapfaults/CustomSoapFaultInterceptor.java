package my.example.customfault.configuration.customsoapfaults;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Optional;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.w3c.dom.Document;

import com.ctc.wstx.exc.WstxException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.UnmarshalException;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.common.FaultConst;
import my.example.customfault.common.SoapUtils;
import my.example.customfault.common.XmlUtils;
import my.example.customfault.logging.SoapFrameworkLogger;

@Slf4j
public class CustomSoapFaultInterceptor extends AbstractSoapInterceptor {

	private static final SoapFrameworkLogger LOG = SoapFrameworkLogger.getLogger(CustomSoapFaultInterceptor.class);

	public CustomSoapFaultInterceptor() {
		super(Phase.PRE_STREAM);
	}

	@Override
	public void handleMessage(SoapMessage soapMessage) throws Fault {

		log.error("Running Custom Interceptor");
		Fault fault = (Fault) soapMessage.getContent(Exception.class);
		Throwable faultCause = fault.getCause();
		String faultMessage = fault.getMessage();
		boolean shouldSwapPayLoad = false;
		if (containsFaultIndicatingNotSchemeCompliantXml(faultCause, faultMessage)) {
			log.error("Fault cause {} ", faultMessage);
			LOG.schemaValidationError(FaultConst.SCHEME_VALIDATION_ERROR, faultMessage);
			shouldSwapPayLoad = true;
			// WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage,
			// FaultConst.SCHEME_VALIDATION_ERROR);
		} else if (containsFaultIndicatingSyntacticallyIncorrectXml(faultCause)) {
			log.error("Fault cause {} ", faultMessage);
			LOG.schemaValidationError(FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR, faultMessage);
			shouldSwapPayLoad = true;
			// WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage,
			// FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
		}

		if (shouldSwapPayLoad) {
			Exchange exchange = soapMessage.getExchange();
			CachedStream cs = new CachedStream();
			soapMessage.setContent(OutputStream.class, cs);
			soapMessage.getInterceptorChain().doIntercept(soapMessage);
			String newPayLoad = null;
			try {
				cs.flush();
				CachedOutputStream csnew = (CachedOutputStream) soapMessage.getContent(OutputStream.class);
				newPayLoad = SoapUtils.replaceFaultNodeAndGetSoapString(csnew.getInputStream());
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			try {
				Message outMessage = Optional.ofNullable(exchange.getOutMessage())
						.orElse(exchange.getOutFaultMessage());
				exchange.setOutMessage(outMessage);
				exchange.setOutFaultMessage(null);

				outMessage.put(Message.RESPONSE_CODE, 200);

				HttpServletResponse response = (HttpServletResponse) outMessage
						.get(AbstractHTTPDestination.HTTP_RESPONSE);
				response.setContentType("text/xml");
				PrintWriter writer = response.getWriter();
				writer.write(newPayLoad);
				outMessage.put(AbstractHTTPDestination.HTTP_RESPONSE, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private boolean containsFaultIndicatingNotSchemeCompliantXml(Throwable faultCause, String faultMessage) {
		if (faultCause instanceof UnmarshalException
				// 1.) If the root-Element of the SoapBody is syntactically correct, but not
				// scheme-compliant,
				// there is no UnmarshalException and we have to look for
				// 2.) Missing / lead to Faults without Causes, but to Messages like "Unexpected
				// wrapper element XYZ found. Expected"
				// One could argue, that this is syntactically incorrect, but here we just take
				// it as Non-Scheme-compliant
				|| isNotNull(faultMessage) && faultMessage.contains("Unexpected wrapper element")) {
			return true;
		}
		return false;
	}

	private boolean containsFaultIndicatingSyntacticallyIncorrectXml(Throwable faultCause) {
		if (faultCause instanceof WstxException
				// If Xml-Header is invalid, there is a wrapped Cause in the original Cause we
				// have to check
				|| isNotNull(faultCause) && faultCause.getCause() instanceof WstxUnexpectedCharException
				|| faultCause instanceof IllegalArgumentException) {
			return true;
		}
		return false;
	}

	private boolean isNotNull(Object object) {
		return object != null;
	}

	private class CachedStream extends CachedOutputStream {
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
