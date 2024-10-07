package my.example.customfault.configuration.customsoapfaults.internal.beans;

import org.apache.commons.chain.Command;

/**
 * Interface for web service business implementations.
 */
public interface IWebService extends Command {

    /**
     * Extract RequestHeader from the SOAPHeader.
     *
     * @param context The context to be processed by this endpoint.
     *
     * @return the service reference id.
     */
    String extractRequestHeader(final WSServiceContext context);

    /**
     * Obtain a new, initialized response document. Subclasses may implement this
     * method, or use method injection to return a document from the Spring container.
     *
     * @return a new response document.
     */
    Object initializeResponse();

    /**
     * Record a general web service failure.
     *
     * @param context the context.
     * @param exception an exception.
     */
    void reportFailure(final WSServiceContext context, final Exception exception);

    /**
     * Update the invocation outcome in the response document with the recorded invocation
     * outcome.
     *
     * @param response the response document.
     * @param context The context to be processed by this endpoint.
     * @param refId the service reference id.
     */
    void updateInvocationOutcome(final Object response, final WSServiceContext context, final String refId);
}
