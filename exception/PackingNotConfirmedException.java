// exception/PackingNotConfirmedException.java
package exception;

/**
 * Exception thrown when order packing is not confirmed.
 * Integrates with SCM Exception Handler for centralized logging and monitoring.
 */
public class PackingNotConfirmedException extends Exception {

    private static final long serialVersionUID = 1L;
    private String errorCode = "PACKING_NOT_CONFIRMED";
    private String severity = "ERROR";
    private long timestamp = System.currentTimeMillis();

    public PackingNotConfirmedException(String msg) {
        super(msg);
    }

    public PackingNotConfirmedException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public PackingNotConfirmedException(String msg, String errorCode, String severity) {
        super(msg);
        this.errorCode = errorCode;
        this.severity = severity;
    }

    public PackingNotConfirmedException(String msg, Throwable cause) {
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