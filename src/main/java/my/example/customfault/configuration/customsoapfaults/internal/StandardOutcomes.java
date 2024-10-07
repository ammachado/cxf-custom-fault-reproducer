package my.example.customfault.configuration.customsoapfaults.internal;

/**
 * Standard invocation outcome identifiers.
 */
public final class StandardOutcomes {
    // CHECKSTYLE:OFF

    /** DOCUMENT ME! */
    public static final String SUCCESSFUL_OUTCOME = "successful";

    /** DOCUMENT ME! */
    public static final int SUCCESSFUL_OUTCOME_CODE = 0;

    /** DOCUMENT ME! */
    public static final String SUCCESSFUL_OUTCOME_MSG = "Successful";

    /** DOCUMENT ME! */
    public static final String ALDSP_SUCCESSFUL_OUTCOME_MSG = "Success";

    /** DOCUMENT ME! */
    public static final String UNAVAIL_OUTCOME = "unavailable";

    /** DOCUMENT ME! */
    public static final int UNAVAIL_OUTCOME_CODE = 100000;

    /** DOCUMENT ME! */
    public static final String UNAVAIL_OUTCOME_MSG = "Service not found or unavailable. Contact Production Support.";

    /** DOCUMENT ME! */
    public static final String UNAUTH_OUTCOME = "unauthorized";

    /** DOCUMENT ME! */
    public static final int UNAUTH_OUTCOME_CODE = 100001;

    /** DOCUMENT ME! */
    public static final String UNAUTH_OUTCOME_MSG = "Not authorized to use service.";

    /** DOCUMENT ME! */
    public static final String SVC_MGR_ERR_OUTCOME = "service.manager.error";

    /** DOCUMENT ME! */
    public static final int SVC_MGR_ERR_OUTCOME_CODE = 100002;

    /** DOCUMENT ME! */
    public static final String SVC_MGR_ERR_OUTCOME_MSG = "Service Manager Error.";

    /** DOCUMENT ME! */
    public static final String SVC_LISTENER_ERR_OUTCOME = "service.listener.error";

    /** DOCUMENT ME! */
    public static final int SVC_LISTENER_ERR_OUTCOME_CODE = 100003;

    /** DOCUMENT ME! */
    public static final String SVC_LISTENER_ERR_OUTCOME_MSG = "Service Listener Error. Request did not complete.";

    /** DOCUMENT ME! */
    public static final String VALIDATION_FAIL_OUTCOME = "validation.failed";

    /** DOCUMENT ME! */
    public static final int VALIDATION_FAIL_OUTCOME_CODE = 101001;

    /** DOCUMENT ME! */
    public static final String VALIDATION_FAIL_OUTCOME_MSG = "Validation Failed.";

    /** DOCUMENT ME! */
    public static final String ALDSP_VALIDATION_FAIL_OUTCOME_MSG = "Validation Failed";

    /** DOCUMENT ME! */
    public static final String SVC_TIMEOUT_OUTCOME = "service.timeout";

    /** DOCUMENT ME! */
    public static final int SVC_TIMEOUT_OUTCOME_CODE = 101002;

    /** DOCUMENT ME! */
    public static final String SVC_TIMEOUT_OUTCOME_MSG = "Service Timed out without completing.";

    /** DOCUMENT ME! */
    public static final String MSG_NOT_RETRIEVED_OUTCOME = "message.not.retrieved";

    /** DOCUMENT ME! */
    public static final int MSG_NOT_RETRIEVED_OUTCOME_CODE = 101003;

    /** DOCUMENT ME! */
    public static final String MSG_NOT_RETRIEVED_OUTCOME_MSG = "Message response was not retrieved by consuming application.";

    /** DOCUMENT ME! */
    public static final String DATA_NOT_FOUND_OUTCOME = "data.not.found";

    /** DOCUMENT ME! */
    public static final int DATA_NOT_FOUND_OUTCOME_CODE = 101004;

    /** DOCUMENT ME! */
    public static final String DATA_NOT_FOUND_OUTCOME_MSG = "Data not found. The service did not find any data for the criteria submitted.";

    /** DOCUMENT ME! */
    public static final int MORE_RESULTS_EXIST_OUTCOME_CODE = 101008;

    /** DOCUMENT ME! */
    public static final String MORE_RESULTS_EXIST_OUTCOME = "more.results.exist";

    /** DOCUMENT ME! */
    public static final String MORE_RESULTS_EXIST_OUTCOME_MSG = "Warning. More results exist than allowed in return.";

    /** DOCUMENT ME! */
    public static final String PARTIAL_DATA_OUTCOME = "partial.data.returned";

    /** DOCUMENT ME! */
    public static final int PARTIAL_DATA_OUTCOME_CODE = 101014;

    /** DOCUMENT ME! */
    public static final String PARTIAL_DATA_OUTCOME_MSG = "Partial Data Returned.";

    /** DOCUMENT ME! */
    public static final String PROC_WITH_ERR_OUTCOME = "processed.with.errors";

    /** DOCUMENT ME! */
    public static final int PROC_WITH_ERR_OUTCOME_CODE = 102000;

    /** DOCUMENT ME! */
    public static final String PROC_WITH_ERR_OUTCOME_MSG = "Processed with Errors.";

    /** DOCUMENT ME! */
    public static final String PROC_WITH_WARN_OUTCOME = "processed.with.warnings";

    /** DOCUMENT ME! */
    public static final int PROC_WITH_WARN_OUTCOME_CODE = 102001;

    /** DOCUMENT ME! */
    public static final String PROC_WITH_WARN_OUTCOME_MSG = "Processed with Warnings.";

    /** DOCUMENT ME! */
    public static final String PROC_VALIDATION_FAIL_OUTCOME = "process.validation.failed";

    /** DOCUMENT ME! */
    public static final int PROC_VALIDATION_FAIL_OUTCOME_CODE = 102002;

    /** DOCUMENT ME! */
    public static final String PROC_VALIDATION_FAIL_OUTCOME_MSG = "Process Validation Failed.";

    /** DOCUMENT ME! */
    public static final String PROC_RULES_FAIL_OUTCOME = "process.rules.failed";

    /** DOCUMENT ME! */
    public static final int PROC_RULES_FAIL_OUTCOME_CODE = 102003;

    /** DOCUMENT ME! */
    public static final String PROC_RULES_FAIL_OUTCOME_MSG = "Process Rules Failed.";

    /** DOCUMENT ME! */
    public static final String FAILURE_OUTCOME = "failure";

    /** DOCUMENT ME! */
    public static final int FAILURE_OUTCOME_CODE = 999999;

    /** DOCUMENT ME! */
    public static final String FAILURE_OUTCOME_MSG = "Operation failed or did not complete successfully.";

    // CHECKSTYLE:ON

    /**
     * Construct a StandardOutcomes object.
     */
    private StandardOutcomes() {
    }
}
