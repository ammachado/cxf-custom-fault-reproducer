package org.apache.cxf.jaxb;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;

public class CignaJaxbCustomValidator implements   ValidationEventHandler{
    private final List<ValidationEvent> events = new ArrayList<ValidationEvent>();

  
    /**
     * Return an array of ValidationEvent objects containing a copy of each of
     * the collected errors and warnings.
     *
     * @return
     *      a copy of all the collected errors and warnings or an empty array
     *      if there weren't any
     */
    public List<ValidationEvent>  getEvents() {
        return events;
    }

    /**
     * Clear all collected errors and warnings.
     */
    public void reset() {
        events.clear();
    }

    /**
     * Returns true if this event collector contains at least one
     * ValidationEvent.
     *
     * @return true if this event collector contains at least one
     *         ValidationEvent, false otherwise
     */
    public boolean hasEvents() {
        return !events.isEmpty();
    }

    @Override
    public boolean handleEvent( ValidationEvent event ) {
        events.add(event);
        System.out.println(event.getMessage());
        return true;
    }

   
}
