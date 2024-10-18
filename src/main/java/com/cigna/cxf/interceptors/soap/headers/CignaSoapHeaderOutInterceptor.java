/**
 * Copyright (c) 2014  CIGNA Corporation. All Rights Reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * CIGNA Corporation. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the agreement you entered into
 * with CIGNA Corporation.
 */
package com.cigna.cxf.interceptors.soap.headers;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Interceptor to set requestHeader to the outbound cxf soap message. 

 */
public class CignaSoapHeaderOutInterceptor extends AbstractSoapInterceptor {

    /** Instance of the logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CignaSoapHeaderOutInterceptor.class);

    /** Request header element namespace */
    public static final String REQ_HEADER_NS = "http://www.cigna.com/utility/1/";

    /** Request header element local part */
    public static final String REQ_HEADER_LN = "requestHeader";


    private static final QName REQUEST_HEADER_QNAME = new QName(REQ_HEADER_NS, REQ_HEADER_LN);


    /** Overwrites the existing Cigna request headers if available in the SoapMessage */
    private boolean overwrite = false;

    /**
     * Creates a new CignaSoaHeaderOutInterceptor object.
     */
    public CignaSoapHeaderOutInterceptor() {
        this(false);
    }

    /**
     * Creates a new CignaSoaHeaderOutInterceptor object.
     */
    public CignaSoapHeaderOutInterceptor(final boolean overwrite) {
        super(Phase.WRITE);
        this.overwrite = overwrite;
    }


    /**
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(final SoapMessage message)
        throws Fault {
        try {
            if (message.getExchange() != null) {
                List<SoapHeader> soapHeaders = (List) message.getExchange().getInMessage().get("org.apache.cxf.headers.Header.list");

                for (SoapHeader soapHeader : soapHeaders) {
                    Element elementName = ((Element) soapHeader.getObject());
                    if (elementName.getLocalName().equalsIgnoreCase(REQ_HEADER_LN)) {
                        message.getHeaders().add(new SoapHeader(REQUEST_HEADER_QNAME, elementName));
                    }
              }
          }
        } catch (Exception e) {
            LOG.error("Failed to set soap header on the outbound request", e);
        }
    }
}
