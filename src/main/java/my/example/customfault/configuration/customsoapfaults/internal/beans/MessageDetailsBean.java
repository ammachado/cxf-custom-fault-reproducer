package my.example.customfault.configuration.customsoapfaults.internal.beans;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The structure covers multiple message details about the processing of this service invocation.
 */
public class MessageDetailsBean implements Serializable {

    /** A logical maximum on the number of detail beans. */
    public static final int MAXIMUM_DETAILS = 50;

    /** The serial version unique identifier for this class. */
    @Serial
    private static final long serialVersionUID = 1000201L;

    /**
     * An array populated with invocation details.
     * Messages pertaining to data validations, warnings, update transactions,
     * and even details that are technical in nature can be captured here.
     * The technical details should be used as pointers for support personnel
     * and not for all the gory system details.
     * MQ errors, other infrastructure messages are also included.
     */
    protected List<MessageDetailBean> messageDetails;

    /**
     * Construct a MessageDetailsBean object.
     */
    public MessageDetailsBean() {
        this.messageDetails = new ArrayList<>();
    }

    /**
     * Construct a MessageDetailsBean object.
     *
     * @param bean Another <code>MessageDetailsBean</code>.
     */
    public MessageDetailsBean(final MessageDetailsBean bean) {
        this();

        Validate.notNull(bean, "bean is null");

        for (MessageDetailBean messageDetail : bean.messageDetails) {
            this.messageDetails.add(new MessageDetailBean(messageDetail));
        }
    }

    /**
     * @return Returns the messageDetails.
     */
    public List<MessageDetailBean> getBoundedMessageDetails() {
        return Collections.unmodifiableList(this.messageDetails.subList(0, Math.min(this.messageDetails.size(), MAXIMUM_DETAILS)));
    }

    /**
     * If the max allowed limit on the messageDetail list is reached this boolean is set to <code>true</code>.
     *
     * @return Return the maxExceeded.
     */
    public boolean isMaxExceeded() {
        return this.messageDetails.size() > MAXIMUM_DETAILS;
    }

    /**
     * @param messageDetails The messageDetails to set.
     */
    public void setMessageDetails(final List<MessageDetailBean> messageDetails) {
        this.messageDetails.clear();
        this.addMessageDetails(messageDetails);
    }

    /**
     * @return Returns the messageDetails.
     */
    public List<MessageDetailBean> getMessageDetails() {
        return Collections.unmodifiableList(this.messageDetails);
    }

    /**
     * Adds a message detail.
     *
     * @param detail The message detail to add.
     */
    public void addMessageDetail(final MessageDetailBean detail) {
        Validate.notNull(detail, "detail is null");
        this.messageDetails.add(new MessageDetailBean(detail));
    }

    /**
     * Adds message details.
     *
     * @param details The message details to add.
     */
    public void addMessageDetails(final Collection<MessageDetailBean> details) {
        Validate.notNull(details, "details is null");
        this.messageDetails.addAll(details);
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
}
