package my.example.customfault.configuration.customsoapfaults;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxb.MyJaxbValidator;
import org.apache.cxf.message.Message;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;

import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailsType;
import jakarta.xml.bind.ValidationEvent;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.common.InterceptingValidationEventHandler;

@Slf4j
public class MyDocLiteralInInterceptor extends DocLiteralInInterceptor {
	public static final String KEEP_PARAMETERS_WRAPPER = MyDocLiteralInInterceptor.class.getName()
			+ ".DocLiteralInInterceptor.keep-parameters-wrapper";

	private static final Logger LOG = LogUtils.getL7dLogger(MyDocLiteralInInterceptor.class);

	
	@Override
	public void handleMessage(Message message) {
		
		System.out.println("Here in doc literal");
		 
		  //To enable to set schema on the unmarshaller
		  message.put(Message.SCHEMA_VALIDATION_ENABLED,SchemaValidationType.REQUEST);
		  
		  message.put(JAXBDataBinding.READER_VALIDATION_EVENT_HANDLER, new MyJaxbValidator());
		
		super.handleMessage(message);
		System.out.println("Here in doc literal after");
		MyJaxbValidator invoked = (MyJaxbValidator)message.get(JAXBDataBinding.READER_VALIDATION_EVENT_HANDLER);
		System.out.println("Here \n\n\n"+invoked.getEvents());
		/*
		 * log.error("invoked"+invoked); String error=""; boolean isError = false; if
		 * (invoked.isHasBeenInvoked()) {
		 * 
		 * MessageDetailsType details = invoked.getRecorder(); List<MessageDetailType>
		 * messageDetails= details.getMessageDetails();
		 * 
		 * for(MessageDetailType messageDetail:messageDetails) { isError = true;
		 * if(error.length()==0) { error+=messageDetail.getTechnicalReturnMessage();
		 * }else { error+="-"+messageDetail.getTechnicalReturnMessage(); } }
		 * 
		 * }
		 */
		List<String> errors = new ArrayList<String>();
		if(invoked.getEvents()!=null && invoked.getEvents().size()>0 ) {
			String error = "";
			for(ValidationEvent event:invoked.getEvents()) {
				if(event.getMessage().contains("cvc-type.3.1.3:")) {
					String trimmedMessage = event.getMessage().replace("cvc-type.3.1.3:", "");
					errors.add(trimmedMessage);
				}
				
			}
			System.out.println(error);
			throw new Fault("SCHEMA ERROR: "+String.join("##--##", errors),LOG);
		}

	}

	private InterceptingValidationEventHandler createEventHandler(Message message) {
		InterceptingValidationEventHandler handler = new InterceptingValidationEventHandler(new MessageDetailsType());

		return handler;
	}

	
}
