package my.example.customfault.configuration.customsoapfaults.internal;

import de.codecentric.namespace.weatherservice.datatypes.InvocationOutcomeType;
import my.example.customfault.configuration.customsoapfaults.internal.beans.WSServiceContext;
import org.apache.commons.chain.Context;

import java.util.Objects;

/**
 * Interface implemented by classes that manage the invocation outcome.
 */
public interface IInvocationOutcomeBuilder {

    /**
     * Retrieve the invocation outcome from the response document.
     *
     * @param response the response document.
     *
     * @return the invocation outcome.
     */
    InvocationOutcomeType getInvocationOutcome(final Object response);

    /**
     * Update the invocation outcome with the service reference id.
     * If the service reference id cannot be found, one will be generated.
     *
     * @param outcome the invocation outcome.
     * @param refId the service reference id.
     */
    default void setServiceRefId(final InvocationOutcomeType outcome, final String refId) {
        Objects.requireNonNull(outcome, "outcome is null");
        outcome.setServiceReferenceId(refId);
    }

    /**
     * Extract RequestHeader from the SOAPHeader.
     *
     * @param context The context to be processed by this endpoint.
     *
     * @return the service reference id.
     */
    String extractRequestHeader(final WSServiceContext context);

    /**
     * Update the invocation outcome in the response document with the recorded invocation
     * outcome.
     *
     * @param response the response document.
     * @param outcome the invocation outcome.
     * @param refid the service reference id.
     */
    void updateInvocationOutcome(final Object response, final InvocationOutcomeType outcome, String refid);
}
