package my.example.customfault.configuration.customsoapfaults.internal;

import org.springframework.stereotype.Component;

import my.example.customfault.configuration.customsoapfaults.internal.beans.IMsgDtlLocator;
import my.example.customfault.configuration.customsoapfaults.internal.beans.MessageDetailBean;

/**
 * Implementation provided per EAP app
 */
@Component
public class MsgDtlLocatorImpl implements IMsgDtlLocator {

    @Override
    public MessageDetailBean getMessageDetail(String type) {
        return switch (type) {
            case StandardOutcomes.SUCCESSFUL_OUTCOME -> new MessageDetailBean(StandardOutcomes.SUCCESSFUL_OUTCOME_CODE, StandardOutcomes.FAILURE_OUTCOME);
            default -> new MessageDetailBean(StandardOutcomes.FAILURE_OUTCOME_CODE, StandardOutcomes.FAILURE_OUTCOME);
        };
    }
}
