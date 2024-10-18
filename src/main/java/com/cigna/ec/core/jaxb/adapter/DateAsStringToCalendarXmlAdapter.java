package com.cigna.ec.core.jaxb.adapter;

import java.util.Calendar;

import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class DateAsStringToCalendarXmlAdapter extends XmlAdapter<String, Calendar> {

	@Override
	public Calendar unmarshal(String v) throws Exception {
		
		return DatatypeConverter.parseDate(v);

	}

	@Override
	public String marshal(Calendar v) throws Exception {

		

		return DatatypeConverter.printDate(v);
	}

}
