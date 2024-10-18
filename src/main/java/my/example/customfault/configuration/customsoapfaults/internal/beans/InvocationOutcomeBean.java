package my.example.customfault.configuration.customsoapfaults.internal.beans;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.codecentric.namespace.weatherservice.datatypes1.InvocationOutcomeType;

/**
 * This class captures the outcome of a service invocation for a single unit of work.
 * It can include classes of services like inquiry, update, task-oriented and process-oriented.
 * The structure provides both an overall outcome and a detailed area for providing further
 * context and should be the first direct child in the root of the overall response.
 * The service response will follow the outcome.
 *
 * Instances of this class must not be shared across threads.
 */
public class InvocationOutcomeBean implements Serializable {

    /** The serial version unique identifier for this class. */
    @Serial
    private static final long serialVersionUID = 1000201L;

    /** Container for an array populated with message details. */
    protected MessageDetailsBean messageDetails;

    /**
     * Overall invocation message.
     * Optional if <code>messageDetails</code> is used to convey multiple invocation outcomes.
     */
    protected String message;

    /**
     * Overall code.
     * Optional if <code>messageDetails</code> is used to convey multiple invocation outcomes.
     */
    protected int code;

    /**
     * Construct an InvocationOutcomeBean object.
     */
    public InvocationOutcomeBean() {
    }

    /**
     * Construct an InvocationOutcomeBean object.
     *
     * @param code The code.
     * @param message The message.
     */
    public InvocationOutcomeBean(final int code, final String message) {
        this.code        = code;
        this.message     = message;
    }

    /**
     * Construct an InvocationOutcomeBean object.
     *
     * @param bean Another <code>InvocationOutcomeBean</code>.
     */
    public InvocationOutcomeBean(final InvocationOutcomeBean bean) {
        Validate.notNull(bean, "bean is null");
        this.code        = bean.code;
        this.message     = bean.message;

        if (bean.messageDetails != null) {
            this.messageDetails = new MessageDetailsBean(bean.messageDetails);
        }
    }

    /**
     * @param code The code to set.
     */
    public void setCode(final int code) {
        this.code = code;
    }

    /**
     * @return Returns the code.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @param messageDetails The messageDetails to set.
     */
    public void setMessageDetails(final MessageDetailsBean messageDetails) {
        if (messageDetails == null) {
            this.messageDetails = null;
        } else {
            this.messageDetails = new MessageDetailsBean(messageDetails);
        }
    }

    /**
     * @return Returns the messageDetails.
     */
    public MessageDetailsBean getMessageDetails() {
        return this.messageDetails;
    }

    /**
     * Adds a message detail.
     *
     * @param detail The message detail to add.
     */
    public void addMessageDetail(final MessageDetailBean detail) {
        Validate.notNull(detail, "detail is null");

        if (this.messageDetails == null) {
            this.messageDetails = new MessageDetailsBean();
        }

        this.messageDetails.addMessageDetail(detail);
    }

    /**
     * Adds message details.
     *
     * @param details The message details to add.
     */
    public void addMessageDetails(final Collection<MessageDetailBean> details) {
        Validate.notNull(details, "details is null");

        if (this.messageDetails == null) {
            this.messageDetails = new MessageDetailsBean();
        }

        this.messageDetails.addMessageDetails(details);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public InvocationOutcomeType toInvocationOutcomeType() {
        return new InvocationOutcomeType(null, this.code, this.message, null);
    }
}
