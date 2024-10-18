package my.example.customfault.endpoint;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.ws.handler.MessageContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyValidationEventHandler implements ValidationEventHandler,jakarta.xml.ws.handler.Handler{
	
	private List<ValidationEvent> events = new ArrayList<ValidationEvent>();
	
	

	public List<ValidationEvent> getEvents() {
		return events;
	}

	public void setEvents(List<ValidationEvent> events) {
		this.events = events;
	}



	@Override
	public boolean handleEvent(ValidationEvent event) {
		        System.out.println("\nEVENT"+event.getLocator());
		        System.out.println("SEVERITY:  " + event.getSeverity());
		        System.out.println("MESSAGE:  " + event.getMessage());
		        System.out.println("LINKED EXCEPTION:  " + event.getLinkedException());
		        System.out.println("LOCATOR");
		        System.out.println("    LINE NUMBER:  " + event.getLocator().getLineNumber());
		        System.out.println("    COLUMN NUMBER:  " + event.getLocator().getColumnNumber());
		        System.out.println("    OFFSET:  " + event.getLocator().getOffset());
		        System.out.println("    OBJECT:  " + event.getLocator().getObject());
		        System.out.println("    NODE:  " + event.getLocator().getNode());
		        System.out.println("    URL:  " + event.getLocator().getURL());
		       
		        getEvents().add(event);
		        System.out.println(getEvents().size());
		        return true;
		    
	}



	@Override
	public boolean handleMessage(MessageContext context) {
		 System.out.println("handleMessage : " +getEvents().size());
		return false;
	}



	@Override
	public boolean handleFault(MessageContext context) {
		 System.out.println("handleFault : " +getEvents().size());
		return false;
	}



	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}
	

	
}
