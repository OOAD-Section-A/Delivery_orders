// exception/InvalidOrderException.java
package exception;

/**
 * Exception thrown when an order validation fails.
 * Integrates with SCM Exception Handler for centralized logging and monitoring.
 */
public class InvalidOrderException extends Exception {

    private static final long serialVersionUID = 1L;
    private String errorCode = "INVALID_ORDER";
    private String severity = "ERROR";
    private long timestamp = System.currentTimeMillis();

    public InvalidOrderException(String msg) {
        super(msg);
    }

    public InvalidOrderException(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public InvalidOrderException(String msg, String errorCode, String severity) {
        super(msg);
        this.errorCode = errorCode;
        this.severity = severity;
    }

    public InvalidOrderException(String msg, Throwable cause) {
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