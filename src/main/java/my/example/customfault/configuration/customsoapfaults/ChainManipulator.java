package my.example.customfault.configuration.customsoapfaults;

import java.util.ListIterator;

import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.helpers.ServiceUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChainManipulator extends AbstractPhaseInterceptor<Message>{

	
	
	public ChainManipulator() {
		super(Phase.POST_PROTOCOL);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		ListIterator<Interceptor<? extends Message>> iterator = message.getInterceptorChain().getIterator();
		Interceptor<? extends Message> interceptor = null;
		while(iterator.hasNext()) {
			Interceptor<? extends Message> next = iterator.next();
			if(next.getClass().getName().equals(DocLiteralInInterceptor.class.getName())) {
				interceptor = next;
			}
		}
		if(interceptor!=null) {
			message.getInterceptorChain().remove(interceptor);
		}
		message.getInterceptorChain().add(new MyDocLiteralInInterceptor());
	}

}
