package my.example.customfault.configuration.customsoapfaults.internal.beans;

/**
 * Service locator interface that describes an invocation outcome lookup.
 */
public interface IOutcomeLocator {

    /**
     * Lookup an invocation outcome instance.
     *
     * @param type the invocation outcome identifier.
     *
     * @return an invocation outcome instance.
     */
    InvocationOutcomeBean getInvocationOutcome(final String type);
}
