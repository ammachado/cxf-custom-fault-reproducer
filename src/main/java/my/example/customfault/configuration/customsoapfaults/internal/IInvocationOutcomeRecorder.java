package my.example.customfault.configuration.customsoapfaults.internal;

import de.codecentric.namespace.weatherservice.datatypes.InvocationOutcomeType;
import de.codecentric.namespace.weatherservice.datatypes.MessageDetailType;
import org.apache.commons.chain.Context;

import java.util.Collection;
import java.util.List;

/**
 * Interface implemented by classes that record invocation outcome.
 */
public interface IInvocationOutcomeRecorder {

    /** Context key used to store the invocation outcome. */
    String OUTCOME_CONTEXT_KEY = IInvocationOutcomeRecorder.class.getName();

    /**
     * Extracts the current invocation outcome from the context.
     *
     * @param context the context.
     *
     * @return the invocation outcome.
     */
    InvocationOutcomeType getOutcomeFromContext(final Context context);

    /**
     * Create a message detail instance with the identifier <code>key</code>.
     *
     * @param key a message detail identifier.
     *
     * @return a message detail instance.
     */
    MessageDetailType createMessageDetail(final String key);

    /**
     * Create a message detail instance with the identifier <code>key</code> and the
     * message <code>message</code>.
     *
     * @param key a message detail identifier.
     * @param message a message.
     *
     * @return a message detail instance.
     */
    MessageDetailType createMessageDetail(final String key, final String message);

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
    MessageDetailType createMessageDetail(final String key, final String id, final String message);

    /**
     * Mark the invocation outcome with a failure status, and set message details
     * from the exception message.
     *
     * @param context the context.
     * @param exception a Throwable.
     */
    void outcomeFailure(final Context context, final Throwable exception);

    /**
     * Mark the invocation outcome with a failure status, and set message details
     * from a list of message details.
     *
     * @param context the context.
     * @param errors a list of message details.
     */
    void outcomeFailure(final Context context, final List<MessageDetailType> errors);

    /**
     * Mark the invocation outcome with a failure status, and set message details
     * from a message detail.
     *
     * @param context the context.
     * @param error a message detail.
     */
    void outcomeFailure(final Context context, final MessageDetailType error);

    /**
     * Mark the invocation outcome with a status key of <code>key</code>, and set message details
     * from a list of message details.
     *
     * @param context the context.
     * @param key a status key
     * @param details a list of message details.
     */
    void outcomeOfType(final Context context, final String key, final Collection<MessageDetailType> details);

    /**
     * Mark the invocation outcome with a status key of <code>key</code>, and set message details
     * from a message detail.
     *
     * @param context the context.
     * @param key a status key
     * @param detail a message detail.
     */
    void outcomeOfType(final Context context, final String key, final MessageDetailType detail);

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
    void outcomeOfType(final Context context, final String key, final int returnCode, final String severityCode,
        final String message);

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
    void outcomeOfType(final Context context, final String key, final int returnCode, final String severityCode, final String id,
        final String message);

    /**
     * Mark the invocation outcome with an externally constructed <code>InvocationOutcomeBean</code>.
     *
     * @param context the context.
     * @param outcome an <code>InvocationOutcomeBean</code>.
     */
    void outcomeOfType(final Context context, final InvocationOutcomeType outcome);

    /**
     * Mark the invocation outcome with a status key of <code>key</code> with no message details.
     *
     * @param context the context.
     * @param key a status key
     */
    void outcomeOfType(final Context context, final String key);

    /**
     * Mark the invocation outcome with a validation error status, and set message details
     * from a list of message details.
     *
     * @param context the context.
     * @param errors a list of message details.
     */
    void outcomeValidationError(final Context context, final Collection<MessageDetailType> errors);

    /**
     * Mark the invocation outcome with a validation error status, and set message details
     * from a message detail.
     *
     * @param context the context.
     * @param error a message detail.
     */
    void outcomeValidationError(final Context context, final MessageDetailType error);
}
