package my.example.customfault.configuration.customsoapfaults.internal.beans;

import de.codecentric.namespace.weatherservice.datatypes.MessageDetailType;
import my.example.customfault.configuration.customsoapfaults.internal.StandardMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * The structure covers a single message detail about
 * the processing of this service invocation.
 */
public class MessageDetailBean implements Serializable {

    /** The serial version unique identifier for this class. */
    @Serial
    private static final long serialVersionUID = 1000201L;

    /**
     * This property is intended mainly for, but not limited to, update type services.
     * For instance, if the update involved changing multiple addresses,
     * a unique identifier from the request payload for each address is placed here.
     * This property when coupled with <code>technicalReturnCode</code> and
     * <code>technicalReturnMessage</code> will convey the outcome of each update transaction.
     */
    protected String id;

    /**
     * A message description of the <code>technicalReturnCode</code>.
     */
    protected String technicalReturnMessage;

    /**
     * This property is intended to provide additional meaning if the intent
     * is to group <code>technicalReturnCode</code>s and their meaning.
     * NS is the default if the code is not set but the provider meant to set it.
     * NS=Not Set, F=Fatal, E=Error, W=Warning, I=Informational.
     */
    protected TechnicalSeverityCodeEnum technicalSeverityCode;

    /**
     * A code that conveys the outcome of the service invocation.
     * Coupled with the <code>id</code> property it conveys the outcome of a specific
     * sub-request. When used without the <code>id</code>, this property can be used
     * for validation messages, warnings, etc.
     */
    protected int technicalReturnCode;

    /**
     * Construct a MessageDetailBean object.
     *
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalReturnMessage The technicalReturnMessage.
     * @param technicalSeverityCode The technicalSeverityCode.
     * @param id The id.
     */
    public MessageDetailBean(final int technicalReturnCode, final String technicalReturnMessage,
        final TechnicalSeverityCodeEnum technicalSeverityCode, final String id) {
        this.technicalReturnCode        = technicalReturnCode;
        this.technicalReturnMessage     = technicalReturnMessage;
        this.id                         = id;
        this.technicalSeverityCode      = technicalSeverityCode;
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalReturnMessage The technicalReturnMessage.
     * @param technicalSeverityCode The technicalSeverityCode.
     */
    public MessageDetailBean(final int technicalReturnCode, final String technicalReturnMessage,
        final TechnicalSeverityCodeEnum technicalSeverityCode) {
        this(technicalReturnCode, technicalReturnMessage, technicalSeverityCode, null);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param throwable The throwable.
     * @param technicalReturnCode The technicalReturnCode.
     */
    public MessageDetailBean(final Throwable throwable, final int technicalReturnCode) {
        this(throwable);
        this.technicalReturnCode = technicalReturnCode;
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param throwable The throwable.
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalSeverityCode The technicalSeverityCode.
     * @param id The id.
     */
    public MessageDetailBean(final Throwable throwable, final int technicalReturnCode,
        final TechnicalSeverityCodeEnum technicalSeverityCode, final String id) {
        this(throwable);

        if (technicalSeverityCode == null) {
            this.technicalSeverityCode = TechnicalSeverityCodeEnum.ERROR;
        } else {
            this.technicalSeverityCode = technicalSeverityCode;
        }

        this.id = id;
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param throwable The throwable.
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalSeverityCode The technicalSeverityCode.
     * @param id The id.
     */
    public MessageDetailBean(final Throwable throwable, final int technicalReturnCode, final String technicalSeverityCode,
        final String id) {
        this(throwable, technicalReturnCode, lookupTechnicalSeverityCodeString(technicalSeverityCode), id);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param throwable The throwable.
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalSeverityCode The technicalSeverityCode.
     */
    public MessageDetailBean(final Throwable throwable, final int technicalReturnCode, final String technicalSeverityCode) {
        this(throwable, technicalReturnCode, technicalSeverityCode, null);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param throwable The throwable.
     */
    public MessageDetailBean(final Throwable throwable) {
        this.technicalReturnCode = StandardMessages.GENERAL_EXCEPTION_ERR_CODE;

        Throwable t = throwable;

        if (throwable instanceof InvocationTargetException exception) {
            t = exception.getCause();
        }

        if (t != null) {
            this.technicalReturnMessage     = t.getMessage();
            this.technicalSeverityCode      = TechnicalSeverityCodeEnum.ERROR;

            if (t instanceof NullPointerException) {
                this.technicalReturnCode = StandardMessages.NULLPOINTER_EXCEPTION_ERR_CODE;
            } else if (t instanceof SQLException) {
                this.technicalReturnCode = StandardMessages.SQL_EXCEPTION_ERR_CODE;
            } else if (t instanceof ClassCastException) {
                this.technicalReturnCode = StandardMessages.CLASSCAST_EXCEPTION_ERR_CODE;
            }
        }
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalReturnMessage The technicalReturnMessage.
     * @param technicalSeverityCode The technicalSeverityCode.
     * @param id The id.
     */
    public MessageDetailBean(final int technicalReturnCode, final String technicalReturnMessage,
        final String technicalSeverityCode, final String id) {
        this(technicalReturnCode, technicalReturnMessage, lookupTechnicalSeverityCodeString(technicalSeverityCode), id);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalReturnMessage The technicalReturnMessage.
     * @param id The id.
     */
    public MessageDetailBean(final int technicalReturnCode, final String technicalReturnMessage, final String id) {
        this(technicalReturnCode, technicalReturnMessage, (TechnicalSeverityCodeEnum) null, id);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param technicalReturnCode The technicalReturnCode.
     * @param technicalReturnMessage The technicalReturnMessage.
     */
    public MessageDetailBean(final int technicalReturnCode, final String technicalReturnMessage) {
        this(technicalReturnCode, technicalReturnMessage, (TechnicalSeverityCodeEnum) null, null);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param technicalReturnCode The technicalReturnCode.
     */
    public MessageDetailBean(final int technicalReturnCode) {
        this(technicalReturnCode, null, (TechnicalSeverityCodeEnum) null, null);
    }

    /**
     * Construct a MessageDetailBean object.
     *
     * @param bean Another <code>MessageDetailBean</code>.
     */
    public MessageDetailBean(final MessageDetailBean bean) {
        Validate.notNull(bean, "bean is null");
        this.id                         = bean.id;
        this.technicalReturnCode        = bean.technicalReturnCode;
        this.technicalReturnMessage     = bean.technicalReturnMessage;
        this.technicalSeverityCode      = bean.technicalSeverityCode;
    }

    /**
     * Construct a MessageDetailBean object.
     */
    public MessageDetailBean() {
        this(0, null, (TechnicalSeverityCodeEnum) null, null);
    }

    /**
     * @param id The id to set.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @param technicalReturnCode The technicalReturnCode to set.
     */
    public void setTechnicalReturnCode(final int technicalReturnCode) {
        this.technicalReturnCode = technicalReturnCode;
    }

    /**
     * @return Returns the technicalReturnCode.
     */
    public int getTechnicalReturnCode() {
        return this.technicalReturnCode;
    }

    /**
     * @param technicalReturnMessage The technicalReturnMessage to set.
     */
    public void setTechnicalReturnMessage(final String technicalReturnMessage) {
        this.technicalReturnMessage = technicalReturnMessage;
    }

    /**
     * @return Returns the technicalReturnMessage.
     */
    public String getTechnicalReturnMessage() {
        return this.technicalReturnMessage;
    }

    /**
     * @param technicalSeverityCode The technicalSeverityCode to set.
     */
    public void setTechnicalSeverityCode(final TechnicalSeverityCodeEnum technicalSeverityCode) {
        this.technicalSeverityCode = technicalSeverityCode;
    }

    /**
     * @return Returns the technicalSeverityCode.
     */
    public TechnicalSeverityCodeEnum getTechnicalSeverityCode() {
        return this.technicalSeverityCode;
    }

    /**
     * @param technicalSeverityCodeString The technicalSeverityCode string value to set.
     */
    public void setTechnicalSeverityCodeString(final String technicalSeverityCodeString) {
        this.technicalSeverityCode = lookupTechnicalSeverityCodeString(technicalSeverityCodeString);
    }

    /**
     * @return Returns the string value of the technicalSeverityCode.
     */
    public String getTechnicalSeverityCodeString() {
        String s = null;

        if (this.technicalSeverityCode != null) {
            s = this.technicalSeverityCode.toString();
        }

        return s;
    }

    /**
     * Factory method that treats the <code>technicalReturnMessage</code> of <code>bean</code>
     * as a <code>MessageFormat</code> pattern. All other properties of <code>bean</code> are
     * copied to the new <code>MessageDetailBean</code> instance.
     *
     * @param bean a <code>MessageDetailBean</code>.
     * @param arguments array of pattern arguments.
     *
     * @return a new <code>MessageDetailBean</code> instance.
     */
    public static MessageDetailBean formattedTechnicalReturnMessageFactory(final MessageDetailBean bean, final Object[] arguments) {
        Validate.notNull(bean, "bean is null");
        Validate.notNull(arguments, "arguments is null");

        final MessageDetailBean ret = new MessageDetailBean(bean);
        ret.setTechnicalReturnMessage(MessageFormat.format(bean.technicalReturnMessage, arguments));
        return ret;
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

    /**
     * Lookup the <code>TechnicalSeverityCodeEnum</code> that corresponds to <code>technicalSeverityCodeString</code>.<p/>
     * Blank or null <code>technicalSeverityCodeString</code> values are allowed, and return a null
     * <code>TechnicalSeverityCodeEnum</code>; other illegal <code>technicalSeverityCodeString</code> values will cause
     * an <code>IllegalArgumentException</code> to be thrown.
     *
     * @param technicalSeverityCodeString The technicalSeverityCode string value look up.
     * @return Returns a <code>TechnicalSeverityCodeEnum</code>, or null as described above.
     */
    private static TechnicalSeverityCodeEnum lookupTechnicalSeverityCodeString(final String technicalSeverityCodeString) {
        TechnicalSeverityCodeEnum tsce = null;

        if (!StringUtils.isBlank(StringUtils.trimToEmpty(technicalSeverityCodeString))) {
            tsce = TechnicalSeverityCodeEnum.getEnum(technicalSeverityCodeString);
            Validate.notNull(tsce, "invalid technicalSeverityCodeString");
        }

        return tsce;
    }

    public MessageDetailType toMessageDetailType() {
        return new MessageDetailType(
                id,
                technicalSeverityCode.toTechnicalSeverityCodeType(),
                technicalReturnCode,
                technicalReturnMessage
        );
    }
}
