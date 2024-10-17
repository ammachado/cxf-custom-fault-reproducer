package my.example.customfault.configuration.customsoapfaults.internal;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.cxf.binding.soap.SoapHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.codecentric.namespace.weatherservice.datatypes.FunctionalContextType;
import de.codecentric.namespace.weatherservice.datatypes.RequestHeaderType;
import de.codecentric.namespace.weatherservice.datatypes.UserPrincipalType;
import de.codecentric.namespace.weatherservice.datatypes1.InvocationOutcomeType;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import my.example.customfault.configuration.customsoapfaults.internal.beans.Lens;
import my.example.customfault.configuration.customsoapfaults.internal.beans.WSServiceContext;

/**
 * Invocation outcome builders for the version 1 schema.
 */
public abstract class InvocationOutcomeBuilderImpl extends AbstractInvocationOutcomeBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvocationOutcomeBuilderImpl.class);

    // CHECKSTYLE:OFF

    /** DOCUMENT ME! */
    private static final String HEADER_NOT_VALID_MSG = "InvocationOutcomeBuilderImpl::extractRequestHeader - invalid RequestHeaderDocument.";

    /** DOCUMENT ME! */
    private static final String HEADER_GENERATED_MSG = "InvocationOutcomeBuilderImpl::extractRequestHeader - RequestHeaderDocument missing or invalid; creating empty document.";

    /** DOCUMENT ME! */
    private static final String SREFID_GENERATED_MSG = "InvocationOutcomeBuilderImpl::extractRequestHeader - blank ServiceReferenceId; generating ServiceReferenceId and placing into RequestHeaderDocument.";

    /** DOCUMENT ME! */
    private static final String SKIPPING_HEADER_MSG = "InvocationOutcomeBuilderImpl::extractRequestHeader - skipping SOAPHeader {0}.";

    /** DOCUMENT ME! */
    private static final String SKIPPING_HEADER_DEBUG_MSG = "InvocationOutcomeBuilderImpl::extractRequestHeader - skipping SOAPHeader {0}: {1}.";

    //functionalContext.userPrincipal.userId"
    private static final Lens<RequestHeaderType, String> userIdLens = Lens.of(RequestHeaderType::getFunctionalContext, RequestHeaderType::withFunctionalContext)
            .andThen(Lens.of(FunctionalContextType::getUserPrincipal, FunctionalContextType::withUserPrincipal))
            .andThen(Lens.of(UserPrincipalType::getUserId, UserPrincipalType::withUserId));
    // CHECKSTYLE:ON

    /**
     * Extract the invocation outcome from the response document.
     *
     * @param response the response document.
     *
     * @return the invocation outcome.
     */
    @Override
    public InvocationOutcomeType getInvocationOutcome(final Object response) {
        Objects.requireNonNull(response, "response is null");

        InvocationOutcomeType invocationOutcome = findOrCreateInvocationOutcome(response);
        if (invocationOutcome == null) {
            log.error("InvocationOutcomeBuilderImpl::getInvocationOutcome - {} not found in response.", response);
        }

        return invocationOutcome;
    }

    /**
     * Extract RequestHeader from the SOAPHeader.
     *
     * @param context The context to be processed by this endpoint.
     *
     * @return the service reference id.
     */
    @Override
    public String extractRequestHeader(final WSServiceContext context) {
        String            refid;
        RequestHeaderType header   = null;
        final Object[]    headers  = context.getSoapHeaders();
        boolean           generate;

        if (headers != null) {
            header = extractRequestHeader(headers);
        }

        generate = header == null;
        context.setGeneratedHeader(generate);

        if (generate) {
            log.warn(HEADER_GENERATED_MSG);
            header = newRequestHeader();

            try {
                final Principal p = context.getPrincipal();

                if (p != null) {
                    userIdLens.set(header, p.getName());
                }
            } catch (final Exception e) {
                throw new IllegalArgumentException("unable to set default userId on generated request header.");
            }
        }

        context.setHeader(header);

        try {
            refid = header.getServiceReferenceId();
        } catch (final Exception e) {
            throw new IllegalArgumentException("header document has no requestHeader.serviceReferenceId property.");
        }

        if (StringUtils.isBlank(refid)) {
            log.warn(SREFID_GENERATED_MSG);
            refid = newServiceReferenceId();
        }

        return refid;
    }

    /**
     * Extract RequestHeaderDocument or RequestHeaderType from the SOAPHeader array.
     *
     * @param headers the array of SOAP Headers.
     *
     * @return the request header or null if not found.
     */
    private RequestHeaderType extractRequestHeader(final Object[] headers) {
        Objects.requireNonNull(headers, "headers is null");

        RequestHeaderType header = null;

        for (int i = 0; i < headers.length && header == null; i++) {

            Element tempHeader = (Element) ((SoapHeader) headers[i]).getObject();

            log.debug("Header value {} ", asString(tempHeader));

            try {

                 headers[i] = header = JAXB.unmarshal(new StringReader(asString(tempHeader)), RequestHeaderType.class);


            } catch (Exception e) {
                log.error(HEADER_NOT_VALID_MSG);
                log.error("Error while setting header {} ", e.getMessage());
                reportSkippedHeader(tempHeader);
            }
        }

        return header;
    }

    /**
     * Report on skipped headers.
     *
     * @param header An Object representing a SOAPHeader
     */
    private void reportSkippedHeader(final Object header) {
        final Class<?> type = header.getClass();
        String           typename;

        if (type == null) {
            typename = "**null schema type**";
        } else {
            final QName name = extractQName(header);

            if (name == null) {
                typename = type.getName();
            } else {
                typename = name.toString();
            }
        }

        if (log.isDebugEnabled()) {
            log.info(MessageFormat.format(SKIPPING_HEADER_DEBUG_MSG, typename, ToStringBuilder.reflectionToString(header)));
        } else {
            log.info(MessageFormat.format(SKIPPING_HEADER_MSG, typename));
        }
    }

    private static QName extractQName(Object obj) {
        try {
            return JAXBContext.newInstance(obj.getClass())
                    .createJAXBIntrospector()
                    .getElementName(obj);
        } catch (JAXBException e) {
            // Not a JAXB object
            return null;
        }
    }

    private static InvocationOutcomeType findOrCreateInvocationOutcome(Object target) {
        if (target == null) {
            return null;
        }

        // Keep a copy of the old value for debugging purposes
        Object oldTarget = target;

        Class<?> clazz = target.getClass();
        System.out.println(clazz);
        for (Field field : clazz.getDeclaredFields()) {
        	
            Class<?> fieldType = field.getType();
            System.out.println(fieldType.getCanonicalName());
            if (fieldType.isPrimitive() || field.getType().getName().startsWith("java.")) {
                continue;
            }

            target = extractFieldValue(field, target);
            if (target instanceof InvocationOutcomeType invocationOutcomeType) {
                return invocationOutcomeType;
            } else if (InvocationOutcomeType.class.equals(fieldType)) {
                InvocationOutcomeType invocationOutcomeType = new InvocationOutcomeType();
                try {
                    FieldUtils.writeField(field, oldTarget, invocationOutcomeType, true);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Unable to set field value for `{}` on class `{}`", field.getName(), target != null ? target.getClass() : null, e);
                }
                return invocationOutcomeType;
            } else if (target != null) {
                return findOrCreateInvocationOutcome(target);
            }
        }

        return null;
    }

    private static Object extractFieldValue(Field field, Object target) {
        try {
        	System.out.println(field.getName()+":--:"+field.getClass().getCanonicalName());
            return FieldUtils.readField(field, target, true);
        } catch (NullPointerException | IllegalAccessException e) {
            return null;
        }
    }

    private static String asString(Node node) {
        StringWriter writer = new StringWriter();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(OutputKeys.VERSION, "1.0");
            if (!(node instanceof Document)) {
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            trans.transform(new DOMSource(node), new StreamResult(writer));
        } catch (final TransformerConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (final TransformerException ex) {
            throw new IllegalArgumentException(ex);
        }
        return writer.toString();
    }
}
