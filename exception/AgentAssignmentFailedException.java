// exception/AgentAssignmentFailedException.java
package exception;

/**
 * Exception thrown when agent assignment fails.
 * Integrates with SCM Exception Handler for centralized logging and monitoring.
 */
public class AgentAssignmentFailedException extends Exception {

    private static final long serialVersionUID = 1L;
    private String errorCode = "AGENT_ASSIGNMENT_FAILED";
    private String severity = "ERROR";
    private long timestamp = System.currentTimeMillis();

    public AgentAssignmentFailedException(String msg) {
        super(msg);
    }

    public AgentAssignmentFailedException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public AgentAssignmentFailedException(String msg, String errorCode, String severity) {
        super(msg);
        this.errorCode = errorCode;
        this.severity = severity;
    }

    public AgentAssignmentFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getSeverity() {
        return severity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s][%s] %s (Time: %d)", errorCode, severity, getMessage(), timestamp);
    }
}