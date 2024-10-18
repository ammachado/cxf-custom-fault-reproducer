package my.example.customfault.configuration.customsoapfaults;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.CignaJaxbCustomValidator;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;

import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailsType;
import jakarta.xml.bind.ValidationEvent;
import lombok.extern.slf4j.Slf4j;
import my.example.customfault.common.InterceptingValidationEventHandler;

@Slf4j
public class CignaDocLiteralInInterceptor extends DocLiteralInInterceptor {
	public static final String KEEP_PARAMETERS_WRAPPER = CignaDocLiteralInInterceptor.class.getName()
			+ ".DocLiteralInInterceptor.keep-parameters-wrapper";

	private static final Logger LOG = LogUtils.getL7dLogger(CignaDocLiteralInInterceptor.class);

	@Override
	public void handleMessage(Message message) {
		
		System.out.println("Here in doc literal");
		
		message.put(JAXBDataBinding.READER_VALIDATION_EVENT_HANDLER, new CignaJaxbCustomValidator());
		super.handleMessage(message);
		CignaJaxbCustomValidator invoked = (CignaJaxbCustomValidator)message.get(JAXBDataBinding.READER_VALIDATION_EVENT_HANDLER);
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
		if(invoked.getEvents()!=null && invoked.getEvents().size()>0 ) {
			String error = "";
			for(ValidationEvent event:invoked.getEvents()) {
				error+=event.getMessage()+"##--##";
				
			}
			System.out.println(error);
			throw new Fault(error,LOG);
		}

	}

	private InterceptingValidationEventHandler createEventHandler(Message message) {
		InterceptingValidationEventHandler handler = new InterceptingValidationEventHandler(new MessageDetailsType());

		return handler;
	}

}
