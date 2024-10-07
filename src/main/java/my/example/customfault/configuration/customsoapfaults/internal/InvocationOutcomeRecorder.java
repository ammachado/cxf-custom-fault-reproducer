package my.example.customfault.configuration.customsoapfaults.internal;

import de.codecentric.namespace.weatherservice.datatypes.InvocationOutcomeType;
import de.codecentric.namespace.weatherservice.datatypes.MessageDetailType;
import de.codecentric.namespace.weatherservice.datatypes.MessageDetailsType;
import de.codecentric.namespace.weatherservice.datatypes.TechnicalSeverityCodeType;
import lombok.Getter;
import lombok.Setter;
import my.example.customfault.configuration.customsoapfaults.internal.beans.IMsgDtlLocator;
import my.example.customfault.configuration.customsoapfaults.internal.beans.IOutcomeLocator;
import my.example.customfault.configuration.customsoapfaults.internal.beans.InvocationOutcomeBean;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Standard invocation outcome recorder implementation.
 */
@Getter
@Setter
public class InvocationOutcomeRecorder implements IInvocationOutcomeRecorder {

    /** The message detail locator. */
    private IMsgDtlLocator msgDtlLocator;

    /** The invocation outcome locator. */
    private IOutcomeLocator outcomeLocator;

    /**
     * Extracts the current invocation outcome from the context.
     *
     * @param context the context.
     *
     * @return the invocation outcome.
     */
    @Override
    public InvocationOutcomeType getOutcomeFromContext(final Context context) {
        Validate.notNull(context, "context is null");

        InvocationOutcomeType outcome;
        final Object          o = context.get(OUTCOME_CONTEXT_KEY);

        if (o == null) {
            InvocationOutcomeBean outcomeFromLookup = getOutcome(StandardOutcomes.SUCCESSFUL_OUTCOME);
            outcome = setOutcomeToContext(context, outcomeFromLookup.toInvocationOutcomeType(), false);
        } else {
            Validate.isTrue(o instanceof InvocationOutcomeType, "outcome is not a " + InvocationOutcomeType.class.getName());
            outcome = (InvocationOutcomeType) o;
        }

        return outcome;
    }

    /**
     * Create a message detail instance with the identifier <code>key</code>.
     *
     * @param key a message detail identifier.
     *
     * @return a message detail instance.
     */
    @Override
    public MessageDetailType createMessageDetail(final String key) {
        return getMessageDetail(key);
    }

    /**
     * Create a message detail instance with the identifier <code>key</code> and the
     * message <code>message</code>.
     *
     * @param key a message detail identifier.
     * @param message a message.
     *
     * @return a message detail instance.
     */
    @Override
    public MessageDetailType createMessageDetail(final String key, final String message) {
        return createMessageDetail(key, null, message);
    }

    /**
     * Create a message detail instance of the identifier <code>key</code>,
     * the reference id <code>id</code>, and the message <code>message</code>.
     *
     * @param key a message detail identifier key.
     * @param id a reference id.
     * @param message a message.
     *
     * @return a message detail instance.
     */
    @Override
    public MessageDetailType createMessageDetail(final String key, final String id, final String message) {
        final MessageDetailType md = getMessageDetail(key);
        md.setId(id);
        md.setTechnicalReturnMessage(message);
        return md;
    }

    /**
     * Mark the invocation outcome with a failure status, and set message details
     * from the exception message.
     *
     * @param context the context.
     * @param exception a throwable.
     */
    @Override
    public void outcomeFailure(final Context context, final Throwable exception) {
        outcomeOfType(context, StandardOutcomes.FAILURE_OUTCOME, createMessageDetailType(exception));
    }

    /**
     * Mark the invocation outcome with a failure status, and set message details
     * from a list of message details.
     *
     * @param context the context.
     * @param errors a list of message details.
     */
    @Override
    public void outcomeFailure(final Context context, final List<MessageDetailType> errors) {
        outcomeOfType(context, StandardOutcomes.FAILURE_OUTCOME, errors);
    }

    /**
     * Mark the invocation outcome with a failure status, and set message details
     * from a message detail.
     *
     * @param context the context.
     * @param error a message detail.
     */
    @Override
    public void outcomeFailure(final Context context, final MessageDetailType error) {
        outcomeOfType(context, StandardOutcomes.FAILURE_OUTCOME, error);
    }

    /**
     * Mark the invocation outcome with an externally constructed <code>InvocationOutcomeBean</code>.
     *
     * @param context the context.
     * @param outcome an <code>InvocationOutcomeBean</code>.
     */
    @Override
    public void outcomeOfType(final Context context, final InvocationOutcomeType outcome) {
        setOutcomeToContext(context, outcome, true);
    }

    /**
     * Mark the invocation outcome with a status key of <code>key</code>, and set message details
     * from a list of message details.
     *
     * @param context the context.
     * @param key a status key
     * @param details a list of message details.
     */
    @Override
    public void outcomeOfType(final Context context, final String key, final Collection<MessageDetailType> details) {
        Validate.notNull(details, "details is null");
        outcomeOfType(context, key);

        final InvocationOutcomeType outcome = getOutcomeFromContext(context);
        details.forEach(detail -> addMessageDetails(outcome, detail));
    }

    /**
     * Mark the invocation outcome with a status key of <code>key</code>, and set message details
     * from a message detail.
     *
     * @param context the context.
     * @param key a status key
     * @param detail a message detail.
     */
    @Override
    public void outcomeOfType(final Context context, final String key, final MessageDetailType detail) {
        Validate.notNull(detail, "detail is null");
        outcomeOfType(context, key);
        addMessageDetail(context, detail);
    }

    /**
     * Mark the invocation outcome with a status key of <code>key</code>, and set message details
     * from message detail elements return code <code>returnCode</code>,
     * severity <code>severityCode</code> and message <code>message</code>.
     *
     * @param context the context.
     * @param key a status key
     * @param returnCode a return code.
     * @param severityCode a severity code.
     * @param message a message.
     */
    @Override
    public void outcomeOfType(final Context context, final String key, final int returnCode, final String severityCode,
        final String message) {
        outcomeOfType(context, key, returnCode, severityCode, null, message);
    }

    /**
     * Mark the invocation outcome with a status key of <code>key</code>, and set message details
     * from message detail elements return code <code>returnCode</code>,
     * severity <code>severityCode</code>, the reference id <code>id</code>, and message <code>message</code>.
     *
     * @param context the context.
     * @param key a status key
     * @param returnCode a return code.
     * @param severityCode a severity code.
     * @param id a reference id.
     * @param message a message.
     */
    @Override
    public void outcomeOfType(final Context context, final String key, final int returnCode, final String severityCode,
        final String id, final String message) {
        final MessageDetailType messageDetailType = new MessageDetailType(
                id,
                TechnicalSeverityCodeType.fromValue(severityCode),
                returnCode,
                message
        );
        outcomeOfType(context, key, messageDetailType);
    }

    /**
     * Mark the invocation outcome with a status key of <code>key</code> with no message details.
     *
     * @param context the context.
     * @param key a status key
     */
    @Override
    public void outcomeOfType(final Context context, final String key) {
        Validate.notNull(context, "context is null");
        Validate.notNull(key, "key is null");
        context.put(OUTCOME_CONTEXT_KEY, getOutcome(key).toInvocationOutcomeType());
    }

    /**
     * Mark the invocation outcome with a validation error status, and set message details
     * from a list of message details.
     *
     * @param context the context.
     * @param errors a list of message details.
     */
    @Override
    public void outcomeValidationError(final Context context, final Collection<MessageDetailType> errors) {
        outcomeOfType(context, StandardOutcomes.VALIDATION_FAIL_OUTCOME, errors);
    }

    /**
     * Mark the invocation outcome with a validation error status, and set message details
     * from a message detail.
     *
     * @param context the context.
     * @param error a message detail.
     */
    @Override
    public void outcomeValidationError(final Context context, final MessageDetailType error) {
        outcomeOfType(context, StandardOutcomes.VALIDATION_FAIL_OUTCOME, error);
    }

    /**
     * Get a message detail instance with the specified key.
     *
     * @param key a message detail key.
     *
     * @return a message detail instance.
     */
    protected MessageDetailType getMessageDetail(final String key) {
        return msgDtlLocator.getMessageDetail(key).toMessageDetailType();
    }

    /**
     * Get an invocation outcome instance with the specified key.
     *
     * @param key an invocation outcome key.
     *
     * @return an invocation outcome instance.
     */
    protected InvocationOutcomeBean getOutcome(final String key) {
        return outcomeLocator.getInvocationOutcome(key);
    }

    /**
     * Add a message detail instance to an invocation outcome instance.
     *
     * @param context the context.
     * @param detail a message detail instance.
     */
    protected void addMessageDetail(final Context context, final MessageDetailType detail) {
        Validate.notNull(context, "context is null");
        Validate.notNull(detail, "detail is null");

        final InvocationOutcomeType outcome = getOutcomeFromContext(context);
        if (outcome.getMessageDetails() == null) {
            outcome.setMessageDetails(new MessageDetailsType());
        }
        outcome.getMessageDetails().getMessageDetail().add(detail);
    }

    /**
     * Places an invocation outcome into the context.
     *
     * @param context the context.
     * @param outcome the invocation outcome.
     * @param copy create a new invocation outcome
     *
     * @return the invocation outcome.
     */
    private InvocationOutcomeType setOutcomeToContext(final Context context, final InvocationOutcomeType outcome,
        final boolean copy) {
        Validate.notNull(context, "context is null");
        Validate.notNull(outcome, "outcome is null");

        InvocationOutcomeType temp = outcome;

        if (copy) {
            MessageDetailsType messageDetails = null;
            temp = new InvocationOutcomeType(null, outcome.getCode(), outcome.getMessage(), messageDetails);
        }

        context.put(OUTCOME_CONTEXT_KEY, temp);
        return temp;
    }

    private static void addMessageDetails(InvocationOutcomeType outcome, MessageDetailType detail) {
        if (outcome.getMessageDetails() == null) {
            outcome.setMessageDetails(new MessageDetailsType());
        }
        outcome.getMessageDetails().getMessageDetail().add(detail);
    }

    private static MessageDetailType createMessageDetailType(final Throwable throwable) {
        int technicalReturnCode = StandardMessages.GENERAL_EXCEPTION_ERR_CODE;

        Throwable t = throwable;

        if (throwable instanceof InvocationTargetException exception) {
            t = exception.getCause();
        }

        String technicalReturnMessage = null;
        TechnicalSeverityCodeType technicalSeverityCode = TechnicalSeverityCodeType.NS;

        if (t != null) {
            technicalReturnMessage = t.getMessage();
            technicalSeverityCode = TechnicalSeverityCodeType.E;

            if (t instanceof NullPointerException) {
                technicalReturnCode = StandardMessages.NULLPOINTER_EXCEPTION_ERR_CODE;
            } else if (t instanceof SQLException) {
                technicalReturnCode = StandardMessages.SQL_EXCEPTION_ERR_CODE;
            } else if (t instanceof ClassCastException) {
                technicalReturnCode = StandardMessages.CLASSCAST_EXCEPTION_ERR_CODE;
            }
        }

        return new MessageDetailType(null, technicalSeverityCode, technicalReturnCode, technicalReturnMessage);
    }
}
