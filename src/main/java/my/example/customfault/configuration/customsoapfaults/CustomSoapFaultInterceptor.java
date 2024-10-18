package my.example.customfault.configuration.customsoapfaults;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.ctc.wstx.exc.WstxException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;

import de.codecentric.namespace.weatherservice.datatypes1.InvocationOutcomeType;
import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailType;
import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailsType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.UnmarshalException;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.common.CxfSoapUtils;
import my.example.customfault.common.FaultConst;
import my.example.customfault.common.InterceptingValidationEventHandler;
import my.example.customfault.common.SoapUtils;
import my.example.customfault.logging.SoapFrameworkLogger;

@Slf4j
public class CustomSoapFaultInterceptor extends AbstractSoapInterceptor {

	private static final SoapFrameworkLogger LOG = SoapFrameworkLogger.getLogger(CustomSoapFaultInterceptor.class);

	public CustomSoapFaultInterceptor() {
		super(Phase.PRE_STREAM);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handleMessage(SoapMessage soapMessage) throws Fault {

		log.error("Running Custom Interceptor");
		Fault fault = (Fault) soapMessage.getContent(Exception.class);
		Throwable faultCause = fault.getCause();
		String faultMessage = fault.getMessage();
		Object respObj = null;
		System.out.println(fault.getMessage());
		boolean shouldSwapPayLoad = false;
		FaultConst faultConst = null;
		
		
		if (StringUtils.isNotEmpty(fault.getMessage() )) {
			
			
			Object newPayLoadObj = checkOuterWrapper(soapMessage, FaultConst.SCHEME_VALIDATION_ERROR,fault.getMessage());

			InputStream inputStream = null;
			try {

				if (newPayLoadObj == null) {

				} else {
					String newPayLoad = null;
					Exchange exchange = soapMessage.getExchange();
					CachedStream cs = new CachedStream();
					soapMessage.setContent(OutputStream.class, cs);
					soapMessage.getInterceptorChain().doIntercept(soapMessage);
					cs.flush();
					CachedOutputStream csnew = (CachedOutputStream) soapMessage.getContent(OutputStream.class);
					inputStream = csnew.getInputStream();

					newPayLoad = SoapUtils.replaceFaultNodeAndGetSoapString(inputStream, newPayLoadObj);
					log.info("New Response {}", newPayLoad);
					Message outMessage = Optional.ofNullable(exchange.getOutMessage())
							.orElse(exchange.getOutFaultMessage());
					exchange.setOutMessage(outMessage);
					exchange.setOutFaultMessage(null);
					outMessage.put(Message.RESPONSE_CODE, 200);
					log.info("{}", outMessage);
					HttpServletResponse response = (HttpServletResponse) outMessage
							.get(AbstractHTTPDestination.HTTP_RESPONSE);

					response.setContentType("text/xml");
					PrintWriter writer = response.getWriter();
					writer.write(newPayLoad);
					outMessage.put(AbstractHTTPDestination.HTTP_RESPONSE, response);
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}

	}

	

	private String getActionFromHeader(Map<String, List<String>> headers) {
		String action = null;
		if (headers != null) {
			List<String> sa = headers.get(SoapBindingConstants.SOAP_ACTION);
			log.info("{}",sa);
			if (sa != null && !sa.isEmpty()) {
				action = sa.get(0);
				if (action.startsWith("\"") || action.startsWith("\'")) {
					action = action.substring(1, action.length() - 1);
					log.debug("Soap Action is {}",action);
				}
			} else {
				log.error("Soap Action could not be determined");
			}
		}
		return action;
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

	/*
	 * This will be the outermost class in the soap body
	 */
	private Object checkOuterWrapper(SoapMessage message, FaultConst faultMessage,String errorMessage) {

		Object outerWrapper = null;
		try {
			Exchange exchange = message.getExchange();
			BindingOperationInfo operation = exchange.getBindingOperationInfo();
			/*
			 * When the error is at root level or soap header level where we cannot
			 * determine which operation to invoke
			 * 
			 */
			if (operation == null) {
				Endpoint endpoint = exchange.getEndpoint();
				Collection<BindingOperationInfo> bops = endpoint.getEndpointInfo().getBinding().getOperations();
				Map<String, List<String>> headers = CastUtils
						.cast((Map<?, ?>) exchange.getInMessage().get(Message.PROTOCOL_HEADERS));
				String action = getActionFromHeader(headers);
				BindingOperationInfo bindingOp = null;
				for (BindingOperationInfo boi : bops) {
					if (CxfSoapUtils.isActionMatch(message, boi, action)) {
						if (bindingOp != null) {
							// more than one op with the same action, will need to parse normally
							return null;
						}
						bindingOp = boi;
					}
					if (CxfSoapUtils.matchWSAAction(boi, action)) {
						if (bindingOp != null && bindingOp != boi) {
							// more than one op with the same action, will need to parse normally
							return null;
						}
						bindingOp = boi;
					}
				}

				if (bindingOp == null) {
					return null;
				} else {
					operation = bindingOp;
				}
				Object outerClass = null;
				List<MessagePartInfo> messageParts = operation.getOperationInfo().getOutput().getMessageParts();
				if (messageParts.size() > 0) {
					// This is the Top Most Class in the Soap Body
					Class<?> typeClass = messageParts.get(0).getMessageInfo().getFirstMessagePart().getTypeClass();
					Method[] methods = typeClass.getMethods();
					Class nextClass = CxfSoapUtils.getNextClass(methods);
					outerClass = ConstructorUtils.invokeConstructor(typeClass, new Object[0]);
					createResponseObject(faultMessage, outerClass, nextClass,errorMessage);
				}
				return outerClass;
			} else {

				List<MessagePartInfo> messageParts = operation.getOperationInfo().getOutput().getMessageParts();

				if (messageParts.size() > 0) {
					// This is the Top Most Class in the Soap Body
					Class<?> typeClass = messageParts.get(0).getMessageInfo().getFirstMessagePart().getTypeClass();
					outerWrapper = ConstructorUtils.invokeConstructor(typeClass, new Object[0]);
					log.info("The class created is {}", outerWrapper.getClass().getCanonicalName());
					Class<?> innerWrapperType = CxfSoapUtils.getInnerWrapper(exchange);
					createResponseObject(faultMessage, outerWrapper, innerWrapperType,errorMessage);

				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return outerWrapper;
	}

	/*
	 * Crearte the Response Object to set in SoapBody
	 */
	private void createResponseObject(FaultConst faultMessage, Object outerWrapper, Class<?> innerWrapperType,String errorMessage)
			throws Exception {

		
		if (outerWrapper.getClass().getMethods() != null) {
			Method[] outerWrapperMethods = outerWrapper.getClass().getMethods();
			Method outerWrapperMethodToInvoke = null;

			for (Method outerWrapperMethod : outerWrapperMethods) {
				// Better checks needed
				if (outerWrapperMethod.getParameterCount() == 1
						&& outerWrapperMethod.getParameterTypes()[0].equals(innerWrapperType)) {
					outerWrapperMethodToInvoke = outerWrapperMethod;
					break;
				}
			}

			Object innerWrapper = ConstructorUtils.invokeConstructor(innerWrapperType, new Object[0]);

			log.info("Inner wrapper is {}", innerWrapper);
			Method[] innerWrapperMethods = innerWrapper.getClass().getMethods();
			Method methodToInvoke = null;
			for (Method innerWrapperMethod : innerWrapperMethods) {
				// Simpler way is to create a setter method
				// need to check if there is a efficient way
				if (innerWrapperMethod.getParameterCount() == 1
						&& innerWrapperMethod.getParameterTypes()[0].equals(InvocationOutcomeType.class)) {
					methodToInvoke = innerWrapperMethod;
				}
			}
			if (methodToInvoke != null) {
				InvocationOutcomeType invocationOutComeType = CxfSoapUtils.createInvocationOutComeType(faultMessage,errorMessage);
				MethodUtils.invokeExactMethod(innerWrapper, methodToInvoke.getName(), invocationOutComeType);
				MethodUtils.invokeExactMethod(outerWrapper, outerWrapperMethodToInvoke.getName(), innerWrapper);
				log.info("The object {}", outerWrapper);
			} else {
				log.info("Methid not avaialbel");
			}

		}

	}
}
