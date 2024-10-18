package com.cigna.ec.core.jaxb.adapter;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class DecimalAsStringToBigDecimalXmlAdapter extends XmlAdapter<String, BigDecimal> {

	@Override
	public BigDecimal unmarshal(String v) throws Exception {
		return jakarta.xml.bind.DatatypeConverter.parseDecimal(v);
	}

	@Override
	public String marshal(BigDecimal v) throws Exception {
		return jakarta.xml.bind.DatatypeConverter.printDecimal(v);
	}

}
