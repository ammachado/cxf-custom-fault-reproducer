package my.example.jaxb.adapter;

import java.util.Calendar;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class DateTimeAsStringToCalendarXmlAdapter extends XmlAdapter<String, Calendar>{

	@Override
	public Calendar unmarshal(String v) throws Exception {
		return jakarta.xml.bind.DatatypeConverter.parseDateTime(v);
	}

	@Override
	public String marshal(Calendar v) throws Exception {
		if(v == null) {
			return null;
		}
		return jakarta.xml.bind.DatatypeConverter.printDateTime(v);
	}

}
