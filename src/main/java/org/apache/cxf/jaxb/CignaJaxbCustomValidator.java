package org.apache.cxf.jaxb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;

public class CignaJaxbCustomValidator implements ValidationEventHandler{
	List<String> errors = null;
	//List<String> errors = new ArrayList<>();
	 //List<String> synchronizedList = Collections.synchronizedList(errors);
	
	public List<String> getErrors(){
		return errors;
	}
	
	public void setErrors(){
		errors = new ArrayList<>();
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
       
        errors.add(event.getMessage());
		return true;
	}

}
