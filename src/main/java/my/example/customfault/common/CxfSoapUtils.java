package my.example.customfault.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.ws.addressing.JAXWSAConstants;

import de.codecentric.namespace.weatherservice.datatypes1.InvocationOutcomeType;
import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailType;
import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailsType;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.configuration.customsoapfaults.internal.StandardOutcomes;

@Slf4j
public final class CxfSoapUtils {
	

	private static final String ALLOW_NON_MATCHING_TO_DEFAULT = "allowNonMatchingToDefaultSoapAction";
	private CxfSoapUtils() {
		
	}
	
	public static boolean isActionMatch(SoapMessage message, BindingOperationInfo boi, String action) {
		SoapOperationInfo soi = boi.getExtensor(SoapOperationInfo.class);
		if (soi == null) {
			return false;
		}
		boolean allowNoMatchingToDefault = MessageUtils.getContextualBoolean(message, ALLOW_NON_MATCHING_TO_DEFAULT,
				false);
		return action.equals(soi.getAction()) || (allowNoMatchingToDefault && StringUtils.isEmpty(soi.getAction())
				|| message.getVersion() instanceof Soap12 && StringUtils.isEmpty(soi.getAction()));
	}

	public static boolean matchWSAAction(BindingOperationInfo boi, String action) {
		Object o = getWSAAction(boi);
		if (o != null) {
			String oa = o.toString();
			if (action.equals(oa) || action.equals(oa + "Request") || oa.equals(action + "Request")) {
				return true;
			}
		}
		return false;
	}

	private static String getWSAAction(BindingOperationInfo boi) {
		Object o =

				boi.getOperationInfo().getInput().getExtensionAttribute(JAXWSAConstants.WSAM_ACTION_QNAME);
		if (o == null) {
			o = boi.getOperationInfo().getInput().getExtensionAttribute(JAXWSAConstants.WSAW_ACTION_QNAME);
		}
		if (o == null) {
			String start = getActionBaseUri(boi.getOperationInfo());
			if (null == boi.getOperationInfo().getInputName()) {
				o = addPath(start, boi.getOperationInfo().getName().getLocalPart());
			} else {
				o = addPath(start, boi.getOperationInfo().getInputName());
			}

		}
		return o.toString();
	}

	private static String getActionBaseUri(final OperationInfo operation) {
		String interfaceName = operation.getInterface().getName().getLocalPart();
		return addPath(operation.getName().getNamespaceURI(), interfaceName);
	}

	private static String getDelimiter(String uri) {
		if (uri.startsWith("urn")) {
			return ":";
		}
		return "/";
	}

	private static String addPath(String uri, String path) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(uri);
		String delimiter = getDelimiter(uri);
		if (!uri.endsWith(delimiter) && !path.startsWith(delimiter)) {
			buffer.append(delimiter);
		}
		buffer.append(path);
		return buffer.toString();
	}
	/*
	 * Better code needed , trying to solve an issue for now
	 */
	public static Class getNextClass(Method[] methods) {

		for (Method method : methods) {
			if (method.getParameterCount() > 0) {
				log.info("Examining Method {} among methods ",method.getName(),methods);
				Parameter[] parameters = method.getParameters();
				for (Parameter param : parameters) {
					log.info("Examining param {}",param.getName());
					if (!param.getType().isPrimitive()) {
						if (param.getType().equals(InvocationOutcomeType.class) ||testParams(Arrays.asList(param.getType().getDeclaredFields()))) {
							return param.getType();
						}
					}
				}
			}

		}
		return null;
	}
	
	public static boolean testParams(List<Field> list) {
		boolean isaMatch = false;
		for(Field f:list) {
			isaMatch = f.getType().equals(InvocationOutcomeType.class);
			if(isaMatch) {
				return isaMatch;
			}
		}
		return false;
	}
	
	/*
	 * Might have multiple inner wrappers , just demo
	 */
	public static  Class<?> getInnerWrapper(Exchange exchange) {

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

	public static  InvocationOutcomeType createInvocationOutComeType( FaultConst faultConst,String errorMessage) {
		List<String> asList = null;
		if(errorMessage!=null) {
			errorMessage = errorMessage.replace("Unmarshalling Error:", "");
			asList = Arrays.asList(errorMessage.split("##--##"));
		}
		MessageDetailsType details = new MessageDetailsType();
		
		if(asList!=null && asList.size()>0) {
			for(String error:asList) {
				MessageDetailType messageDetail = new MessageDetailType();
				messageDetail.setTechnicalReturnMessage(error);
				details.getMessageDetails().add(messageDetail);
			}
		}
		
		InvocationOutcomeType outcome = new InvocationOutcomeType();
		outcome.setCode(StandardOutcomes.VALIDATION_FAIL_OUTCOME_CODE);
		outcome.setMessage(faultConst.getMessage());
		outcome.setServiceReferenceId(UUID.randomUUID().toString());
		outcome.setMessageDetails(details);
		return outcome;

	}

}
