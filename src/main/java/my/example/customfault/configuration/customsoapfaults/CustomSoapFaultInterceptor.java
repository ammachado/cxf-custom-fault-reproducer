package my.example.customfault.configuration.customsoapfaults;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.databinding.WrapperHelper;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.ctc.wstx.exc.WstxException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;

import de.codecentric.namespace.weatherservice.datatypes.InvocationOutcomeType;
import de.codecentric.namespace.weatherservice.datatypes.MessageDetailType;
import de.codecentric.namespace.weatherservice.datatypes.MessageDetailsType;
import de.codecentric.namespace.weatherservice.datatypes.TechnicalSeverityCodeType;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import de.codecentric.namespace.weatherservice.general.GetCityForecastByZIPResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.UnmarshalException;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.common.FaultConst;
import my.example.customfault.common.SoapUtils;
import my.example.customfault.configuration.customsoapfaults.internal.InvocationOutcomeBuilderImpl;
import my.example.customfault.configuration.customsoapfaults.internal.StandardOutcomes;
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

		boolean shouldSwapPayLoad = false;
		if (containsFaultIndicatingNotSchemeCompliantXml(faultCause, faultMessage)) {
			log.error("Fault cause {} ", faultMessage);
			LOG.schemaValidationError(FaultConst.SCHEME_VALIDATION_ERROR, faultMessage);
			faultMessage = FaultConst.SCHEME_VALIDATION_ERROR.getMessage();
			shouldSwapPayLoad = true;
			// WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage,
			// FaultConst.SCHEME_VALIDATION_ERROR);
		} else if (containsFaultIndicatingSyntacticallyIncorrectXml(faultCause)) {
			log.error("Fault cause {} ", faultMessage);
			LOG.schemaValidationError(FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR, faultMessage);
			faultMessage = FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR.getMessage();
			shouldSwapPayLoad = true;
			// WeatherSoapFaultHelper.buildWeatherFaultAndSet2SoapMessage(soapMessage,
			// FaultConst.SYNTACTICALLY_INCORRECT_XML_ERROR);
		} else if (faultMessage != null && faultMessage.contains("Does it exist in service WSDL")) {
			faultMessage = FaultConst.SCHEME_VALIDATION_ERROR.getMessage();
			log.warn("*****************************************");
			shouldSwapPayLoad = true;
		}

		if (shouldSwapPayLoad) {

			Object newPayLoadObj = checkOuterWrapper(soapMessage, faultMessage);

			InputStream inputStream = null;
			try {

				if (newPayLoadObj == null) {
					log.error("New PayLoad could not be obtained");
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
					log.info("New Response {}",newPayLoad);
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
	private Object checkOuterWrapper(SoapMessage message, String faultMessage) {

		if (message.getVersion() instanceof Soap11) {
			Map<String, List<String>> headers = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
			if (headers != null) {
				List<String> sa = headers.get(SoapBindingConstants.SOAP_ACTION);
				if (sa != null && !sa.isEmpty()) {
					String action = sa.get(0);
					if (action.startsWith("\"") || action.startsWith("\'")) {
						action = action.substring(1, action.length() - 1);
					}
					System.out.println("\n\n\n\n\n\n\n action is:" + action + "\n\n\n\n\n");
				} else {
					System.out.println("\n\n\n\n\n\n\n action ignored \n\n\n\n\n");
				}
			} else {
				
				Exchange exchange = message.getExchange();
				log.info("{}",exchange.getDestination());
				Destination destination = exchange.getDestination();
				
				log.info("{}",exchange.getBinding().getBindingInfo().getService().getTopLevelDoc());

			}
		} else {
			System.out.println("\n\n\n\n\n\n\n action isgone \n\n\n\n\n");
		}

		Object outerWrapper = null;
		try {
			Exchange exchange = message.getExchange();
			BindingOperationInfo operation = exchange.getBindingOperationInfo();

			if (operation == null) {
				log.error("BindingOperation not obtained");
				return null;
			}
			log.info("Binding operation is {}", operation);

			final List<MessagePartInfo> parts;
			final BindingMessageInfo bmsg;
			boolean client = isRequestor(message);

			if (operation.getOutput() != null) {
				bmsg = operation.getOutput();
				parts = bmsg.getMessageParts();
				log.info("{}", bmsg);
				log.info("{}", parts);
			}
			log.info("{}\n{}", operation.getOperationInfo().getOutput(),
					operation.getOperationInfo().getOutput().getMessageParts());
			List<MessagePartInfo> messageParts = operation.getOperationInfo().getOutput().getMessageParts();
			log.info("{}", messageParts.size());

			if (messageParts.size() > 0) {
				// This is the Top Most Class in the Soap Body
				Class<?> typeClass = messageParts.get(0).getMessageInfo().getFirstMessagePart().getTypeClass();
				outerWrapper = ConstructorUtils.invokeConstructor(typeClass, new Object[0]);
				log.info("The class created is {}", outerWrapper.getClass().getCanonicalName());
				if (outerWrapper.getClass().getMethods() != null) {
					Method[] outerWrapperMethods = outerWrapper.getClass().getMethods();
					Method outerWrapperMethodToInvoke = null;
					Class<?> innerWrapperType = getInnerWrapper(exchange);

					for (Method outerWrapperMethod : outerWrapperMethods) {
						// Better checks needed
						if (outerWrapperMethod.getParameterCount() == 1
								&& outerWrapperMethod.getParameterTypes()[0].equals(innerWrapperType)) {
							outerWrapperMethodToInvoke = outerWrapperMethod;
							break;
						}
					}

					Object innerWrapper = ConstructorUtils.invokeConstructor(getInnerWrapper(exchange), new Object[0]);
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
						InvocationOutcomeType invocationOutComeType = createInvocationOutComeType(message,
								faultMessage);
						MethodUtils.invokeExactMethod(innerWrapper, methodToInvoke.getName(), invocationOutComeType);
						MethodUtils.invokeExactMethod(outerWrapper, outerWrapperMethodToInvoke.getName(), innerWrapper);
						log.info("The object {}", outerWrapper);
					} else {
						log.info("Methid not avaialbel");
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return outerWrapper;
	}

	/*
	 * Might have multiple inner wrappers , just demo
	 */
	private Class<?> getInnerWrapper(Exchange exchange) {

		BindingOperationInfo bop = exchange.getBindingOperationInfo();
		BindingOperationInfo newbop = bop.getWrappedOperation();
		BindingMessageInfo output = newbop.getOutput();
		MessageInfo wrappedMsgInfo = newbop.getOutput().getMessageInfo();

		Class<?> wrapped = null;
		if (wrappedMsgInfo.getMessagePartsNumber() > 0) {
			log.info("{}", output.getMessageParts());
			int messagePartsNumber = wrappedMsgInfo.getMessagePartsNumber();
			wrapped = wrappedMsgInfo.getFirstMessagePart().getTypeClass();
		}
		return wrapped;
	}

	private InvocationOutcomeType createInvocationOutComeType(SoapMessage soapMessage, String faultMessage) {

		MessageDetailType messageDetail = new MessageDetailType();
		messageDetail.setTechnicalReturnMessage(StandardOutcomes.VALIDATION_FAIL_OUTCOME_MSG);
		MessageDetailsType details = new MessageDetailsType();
		details.getMessageDetail().add(messageDetail);
		InvocationOutcomeType outcome = new InvocationOutcomeType();
		outcome.setCode(StandardOutcomes.VALIDATION_FAIL_OUTCOME_CODE);
		outcome.setMessage(faultMessage);
		outcome.setServiceReferenceId(UUID.randomUUID().toString());
		outcome.setMessageDetails(details);

		return outcome;

	}
}
