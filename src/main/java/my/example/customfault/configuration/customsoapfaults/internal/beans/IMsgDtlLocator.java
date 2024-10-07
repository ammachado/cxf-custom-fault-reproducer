package my.example.customfault.configuration.customsoapfaults.internal.beans;

/**
 * Service locator interface that describes a message detail lookup.
 */
public interface IMsgDtlLocator {

    /**
     * Lookup a message detail instance.
     *
     * @param type the message detail identifier.
     *
     * @return a message detail instance.
     */
    MessageDetailBean getMessageDetail(final String type);
}
