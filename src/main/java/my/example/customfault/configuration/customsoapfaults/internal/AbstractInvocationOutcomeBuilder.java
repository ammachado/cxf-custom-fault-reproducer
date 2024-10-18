package my.example.customfault.configuration.customsoapfaults.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import de.codecentric.namespace.weatherservice.datatypes.FunctionalContextType;
import de.codecentric.namespace.weatherservice.datatypes.RequestHeader;
import de.codecentric.namespace.weatherservice.datatypes.TargetType;
import de.codecentric.namespace.weatherservice.datatypes.UserPrincipalType;
import de.codecentric.namespace.weatherservice.datatypes1.InvocationOutcomeType;
import lombok.Setter;

/**
 * Superclass for all invocation outcome builders.
 */
public abstract class AbstractInvocationOutcomeBuilder implements IInvocationOutcomeBuilder {

    private static final String UNAUTHENTICATED = "UNAUTHENTICATED";
    private static final String UNKNOWN = "UNKNOWN";

    /** Default maximum error count. */
    private static final int DEFAULT_MAX_ERRORS = 50;

    /** The log. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Configured maximum error count. */
    @Setter
    protected int maximumErrors = DEFAULT_MAX_ERRORS;

    /**
     * Obtain a new, initialized request header document. Subclasses may implement this
     * method, or use method injection to return a document from the Spring container.
     *
     * @return a new request header document.
     */
    public RequestHeader newRequestHeader() {
        final de.codecentric.namespace.weatherservice.datatypes.ObjectFactory objectFactory =
                new de.codecentric.namespace.weatherservice.datatypes.ObjectFactory();

        final UserPrincipalType userPrincipalType = objectFactory.createUserPrincipalType()
                .withUserId(UNAUTHENTICATED)
                .withOrgRole(UNAUTHENTICATED)
                .withAppRole(UNAUTHENTICATED);

        final TargetType targetType = objectFactory.createTargetType()
                .withServiceName(UNKNOWN)
                .withServiceOperation(UNKNOWN);

        final FunctionalContextType functionalContextType = objectFactory.createFunctionalContextType()
                .withConsumerName(UNKNOWN)
                .withUserPrincipal(userPrincipalType)
                .withTarget(targetType);

        return objectFactory.createRequestHeader()
                .withServiceReferenceId(newServiceReferenceId())
                .withFunctionalContext(functionalContextType);
    }

    /**
     * Obtain a new service reference id. Subclasses may implement this
     * method, or use method injection to return a document from the Spring container.
     *
     * @return a new service reference id.
     */
    public abstract String newServiceReferenceId();

    /**
     * Update the invocation outcome in the response document with the recorded invocation
     * outcome.
     *
     * @param response the response document.
     * @param outcome the invocation outcome.
     * @param refId the service reference id.
     */
    @Override
    public void updateInvocationOutcome(final Object response, final InvocationOutcomeType outcome, final String refId) {
        if (response == null || outcome == null) {
            log.warn("AbstractInvocationOutcomeBuilder::updateInvocationOutcome - response or outcome are null.");
        } else {
            InvocationOutcomeType outcomeFromResponse = getInvocationOutcome(response);
            setServiceRefId(outcomeFromResponse, refId);

            /*if (outcome.getCode() != StandardOutcomes.SUCCESSFUL_OUTCOME_CODE) {*/
                BeanUtils.copyProperties(outcome, outcomeFromResponse);
            /*}*/
        }
    }
}
