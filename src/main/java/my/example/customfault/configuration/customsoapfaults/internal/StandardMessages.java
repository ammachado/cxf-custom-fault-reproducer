package my.example.customfault.configuration.customsoapfaults.internal;

/**
 * Standard message detail identifiers.
 */
public final class StandardMessages {
    // CHECKSTYLE:OFF

    /** DOCUMENT ME! */
    public static final String SYNTAX_ERR = "syntax.error";

    /** DOCUMENT ME! */
    public static final int SYNTAX_ERR_CODE = 100004;

    /** DOCUMENT ME! */
    public static final String REQUIRED_FIELD_ERR = "required.field.missing";

    /** DOCUMENT ME! */
    public static final int REQUIRED_FIELD_ERR_CODE = 103001;

    /** DOCUMENT ME! */
    public static final String DATA_TYPE_ERR = "invalid.data.type";

    /** DOCUMENT ME! */
    public static final int DATA_TYPE_ERR_CODE = 103002;

    /** DOCUMENT ME! */
    public static final String DATA_LEN_ERR = "invalid.data.len";

    /** DOCUMENT ME! */
    public static final int DATA_LEN_ERR_CODE = 103003;

    /** DOCUMENT ME! */
    public static final String DATA_TOO_LONG_ERR = "data.element.too.long";

    /** DOCUMENT ME! */
    public static final int DATA_TOO_LONG_ERR_CODE = 103004;

    /** DOCUMENT ME! */
    public static final String DATE_ERR = "invalid.date";

    /** DOCUMENT ME! */
    public static final int DATE_ERR_CODE = 103005;

    /** DOCUMENT ME! */
    public static final String EFF_GT_CAN_DATE_ERR = "effdt.gt.candt";

    /** DOCUMENT ME! */
    public static final int EFF_GT_CAN_DATE_ERR_CODE = 103006;

    /** DOCUMENT ME! */
    public static final String CODE_ERR = "invalid.code";

    /** DOCUMENT ME! */
    public static final int CODE_ERR_CODE = 103007;

    /** DOCUMENT ME! */
    public static final String CONDITIONAL_MISSING_ERR = "conditional.data.missing";

    /** DOCUMENT ME! */
    public static final int CONDITIONAL_MISSING_ERR_CODE = 103008;

    /** DOCUMENT ME! */
    public static final String MUTUAL_EXCLUSIVE_ERR = "mutually.exclusive";

    /** DOCUMENT ME! */
    public static final int MUTUAL_EXCLUSIVE_ERR_CODE = 103009;

    /** DOCUMENT ME! */
    public static final String GENERAL_EXCEPTION_ERR = "exception.general";

    /** DOCUMENT ME! */
    public static final int GENERAL_EXCEPTION_ERR_CODE = 500000;

    /** DOCUMENT ME! */
    public static final String SQL_EXCEPTION_ERR = "exception.sql";

    /** DOCUMENT ME! */
    public static final int SQL_EXCEPTION_ERR_CODE = 500020;

    /** DOCUMENT ME! */
    public static final String QUERY_EXCEPTION_ERR = "exception.query";

    /** DOCUMENT ME! */
    public static final int QUERY_EXCEPTION_ERR_CODE = 502000;

    /** DOCUMENT ME! */
    public static final String NULLPOINTER_EXCEPTION_ERR = "exception.npe";

    /** DOCUMENT ME! */
    public static final int NULLPOINTER_EXCEPTION_ERR_CODE = 503000;

    /** DOCUMENT ME! */
    public static final String CLASSCAST_EXCEPTION_ERR = "exception.cast";

    /** DOCUMENT ME! */
    public static final int CLASSCAST_EXCEPTION_ERR_CODE = 503200;

    /** DOCUMENT ME! */
    public static final String FATAL_ERR = "fatal.error";

    /** DOCUMENT ME! */
    public static final int FATAL_ERR_CODE = 999999;

    // CHECKSTYLE:ON

    /**
     * Construct a StandardMessages object.
     */
    private StandardMessages() {
    }
}
