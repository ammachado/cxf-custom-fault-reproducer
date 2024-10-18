package my.example.customfault.configuration.customsoapfaults.internal.beans;

import java.io.Serial;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.impl.ContextBase;

import de.codecentric.namespace.weatherservice.datatypes.RequestHeader;
import jakarta.activation.DataHandler;
import lombok.Getter;
import lombok.Setter;
import my.example.customfault.configuration.customsoapfaults.internal.IInvocationOutcomeRecorder;

/**
 * A Context implementation for Web Services.
 */
@Getter
@Setter
public class WSServiceContext extends ContextBase {

    /**
     * The serial version unique identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 1020002L;

    /**
     * The current service.
     */
    private IWebService service;

    /**
     * The caller principal.
     */
    private Principal principal;

    /**
     * The service id.
     */
    private String serviceId;

    /**
     * The service reference id.
     */
    private String serviceReferenceId;

    /**
     * The request header.
     */
    private RequestHeader header;

    /**
     * The web service request data.
     */
    private Object request;

    /**
     * The web service response data.
     */
    private Object response;

    /**
     * The SOAP request attachments.
     */
    private DataHandler[] requestAttachments;

    /**
     * The SOAP response attachments.
     */
    private DataHandler[] responseAttachments;

    /**
     * The SOAPHeaders.
     */
    private Object[] soapHeaders;

    /**
     * The request header is generated.
     */
    private boolean generatedHeader;

    /**
     * Construct a WSServiceContext object.
     */
    public WSServiceContext() {
        super();
    }

    /**
     * Construct a WSServiceContext object.
     *
     * @param map a map of context parameters.
     */
    public WSServiceContext(final Map map) {
        super(map);
        this.remove(IInvocationOutcomeRecorder.OUTCOME_CONTEXT_KEY);
    }

    /**
     * Return the response.
     *
     * @return Returns the response.
     */
    public Object getResponse() {
        if (this.response == null) {
            this.response = service.initializeResponse();
        }

        return this.response;
    }

    /**
     * Add a response attachment.
     *
     * @param attachment the attachment.
     * @param mimeType   the MIME type.
     */
    public void addResponseAttachment(final Object attachment, final String mimeType) {
        addResponseAttachment(new DataHandler(attachment, mimeType));
    }

    /**
     * Add a response attachment.
     *
     * @param attachment the attachment.
     */
    public void addResponseAttachment(final DataHandler attachment) {
        final List<DataHandler> temp = new ArrayList<>();

        if (this.responseAttachments != null) {
            temp.addAll(Arrays.asList(this.responseAttachments));
        }

        temp.add(attachment);
        this.responseAttachments = temp.toArray(new DataHandler[0]);
    }

    /**
     * Clear the array of response attachments.
     */
    public void clearResponseAttachments() {
        setResponseAttachments(null);
    }
}
