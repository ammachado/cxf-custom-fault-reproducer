package my.example.customfault.configuration.customsoapfaults.internal;

import my.example.customfault.configuration.customsoapfaults.internal.beans.IOutcomeLocator;
import my.example.customfault.configuration.customsoapfaults.internal.beans.InvocationOutcomeBean;

/**
 * Implementation provided per EAP app
 */
public class OutcomeLocatorImpl implements IOutcomeLocator {

    @Override
    public InvocationOutcomeBean getInvocationOutcome(String type) {
        return switch (type) {
            case StandardOutcomes.SUCCESSFUL_OUTCOME -> new InvocationOutcomeBean(StandardOutcomes.SUCCESSFUL_OUTCOME_CODE, StandardOutcomes.FAILURE_OUTCOME);
            default -> new InvocationOutcomeBean(StandardOutcomes.FAILURE_OUTCOME_CODE, StandardOutcomes.FAILURE_OUTCOME);
        };
    }
}
