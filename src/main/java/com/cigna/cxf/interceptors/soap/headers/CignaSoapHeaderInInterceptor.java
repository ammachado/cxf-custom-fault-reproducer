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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * Interceptor to set requestHeader to the outbound cxf soap message. 

 */
public class CignaSoapHeaderInInterceptor extends AbstractSoapInterceptor {

    /** Instance of the logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CignaSoapHeaderInInterceptor.class);


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
    public CignaSoapHeaderInInterceptor() {
        this(false);
    }

    /**
     * Creates a new CignaSoaHeaderOutInterceptor object.
     */
    public CignaSoapHeaderInInterceptor(final boolean overwrite) {
        super(Phase.READ);

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
                Map<String, List<String>> headers = CastUtils.cast((Map)message.get(Message.PROTOCOL_HEADERS));
                if (headers != null) {
                    List<String> sa = headers.get("SOAPAction");
                    if (sa != null && sa.size() > 0) {
                        String action = sa.get(0);
                            LOG.debug("The Soap Action recieved: {}", action);
                    }

                    LOG.debug("Removing SoapAction Header ");
                    headers.remove("SOAPAction");
                }
          }
        } catch (Exception e) {
            LOG.error("Failed to set soap header on the outbound request", e);
        }
    }
}
