package my.example.customfault.configuration.customsoapfaults.internal.beans;

import de.codecentric.namespace.weatherservice.datatypes.TechnicalSeverityCodeType;

import java.io.Serial;
import java.util.Arrays;

/**
 * This class is intended to provide additional meaning if the intent
 * is to group technical return codes and their meaning.
 * NS is the default if the code is not set but the provider meant to set it.
 * NS=Not Set, F=Fatal, E=Error, W=Warning, I=Informational.
 */
public enum TechnicalSeverityCodeEnum {

    /** Technical Severity Code NS. */
    NOT_SET("NS"),

    /** Technical Severity Code F. */
    FATAL("F"),

    /** Technical Severity Code E. */
    ERROR("E"),

    /** Technical Severity Code W. */
    WARNING("W"),

    /** Technical Severity Code I. */
    INFORMATIONAL("I");

    /** The serial version unique identifier for this class. */
    @Serial
    private static final long serialVersionUID = 1000201L;

    private final String code;

    /**
     * Construct a TechnicalSeverityCodeEnum object.
     *
     * @param code DOCUMENT ME!
     */
    private TechnicalSeverityCodeEnum(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public TechnicalSeverityCodeType toTechnicalSeverityCodeType() {
        return switch (this){
            case NOT_SET -> TechnicalSeverityCodeType.NS;
            case FATAL -> TechnicalSeverityCodeType.F;
            case ERROR -> TechnicalSeverityCodeType.E;
            case WARNING -> TechnicalSeverityCodeType.W;
            case INFORMATIONAL -> TechnicalSeverityCodeType.I;
        };
    }

    /**
     * Gets an TechnicalSeverityCodeEnum object by code.
     *
     * @param code the code of the <code>TechnicalSeverityCodeEnum</code> to get, may be null
     *
     * @return the <code>TechnicalSeverityCodeEnum</code> object,
     * or null if the <code>TechnicalSeverityCodeEnum</code> does not exist.
     */
    public static TechnicalSeverityCodeEnum getEnum(final String code) {
        return Arrays.stream(values())
                .filter(x -> x.getCode().equals(code))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Invalid code: %s".formatted(code)));
    }
}
