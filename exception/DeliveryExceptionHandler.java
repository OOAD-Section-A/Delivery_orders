// exception/DeliveryExceptionHandler.java
package exception;

import com.jackfruit.scm.exception.SCMExceptionHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized Exception Handler for Delivery Subsystem.
 * Integrates with SCM Exception Handler (scm-exception-handler-v3.jar)
 * for unified logging, monitoring, and error tracking across the delivery
 * system.
 */
public class DeliveryExceptionHandler {

    private static final SCMExceptionHandler scmHandler = new SCMExceptionHandler();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static boolean loggingEnabled = true;
    private static boolean scmIntegrationEnabled = true;

    /**
     * Handles delivery-specific exceptions with centralized logging.
     * Logs to SCM Exception Handler for monitoring and analysis.
     */
    public static void handleDeliveryException(Exception e, String context) {
        if (!loggingEnabled)
            return;

        String timestamp = LocalDateTime.now().format(formatter);
        String errorMessage = String.format(
                "[%s] DELIVERY SUBSYSTEM ERROR | Context: %s | Error: %s | Type: %s",
                timestamp,
                context,
                e.getMessage(),
                e.getClass().getSimpleName());

        // Log to console
        System.err.println(errorMessage);

        // Send to SCM Exception Handler if enabled
        if (scmIntegrationEnabled) {
            try {
                scmHandler.logException(e, context, "DELIVERY", getSeverity(e));
            } catch (Exception scmEx) {
                System.err.println("⚠️  SCM Exception Handler unavailable: " + scmEx.getMessage());
            }
        }
    }

    /**
     * Logs a custom exception with details.
     */
    public static void logException(Exception e, String subsystem, String severity) {
        if (!loggingEnabled)
            return;

        try {
            if (scmIntegrationEnabled) {
                scmHandler.logException(e, subsystem, subsystem, severity);
            }
        } catch (Exception ex) {
            System.err.println("⚠️  Failed to log exception: " + ex.getMessage());
        }
    }

    /**
     * Handles InvalidOrderException specifically.
     */
    public static void handleInvalidOrderException(InvalidOrderException e) {
        handleDeliveryException(e, "ORDER_VALIDATION");
    }

    /**
     * Handles PackingNotConfirmedException specifically.
     */
    public static void handlePackingException(PackingNotConfirmedException e) {
        handleDeliveryException(e, "PACKING_VALIDATION");
    }

    /**
     * Handles AgentAssignmentFailedException specifically.
     */
    public static void handleAgentAssignmentException(AgentAssignmentFailedException e) {
        handleDeliveryException(e, "AGENT_ASSIGNMENT");
    }

    /**
     * Determines severity level based on exception type.
     */
    private static String getSeverity(Exception e) {
        if (e instanceof InvalidOrderException) {
            return ((InvalidOrderException) e).getSeverity();
        } else if (e instanceof PackingNotConfirmedException) {
            return ((PackingNotConfirmedException) e).getSeverity();
        } else if (e instanceof AgentAssignmentFailedException) {
            return ((AgentAssignmentFailedException) e).getSeverity();
        }
        return "ERROR";
    }

    /**
     * Sets whether exception logging is enabled.
     */
    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    /**
     * Sets whether SCM Integration is enabled.
     */
    public static void setSCMIntegrationEnabled(boolean enabled) {
        scmIntegrationEnabled = enabled;
    }

    /**
     * Gets the underlying SCM Exception Handler for advanced operations.
     */
    public static SCMExceptionHandler getSCMHandler() {
        return scmHandler;
    }

    /**
     * Initializes the exception handler with SCM system.
     */
    public static void initialize() {
        System.out.println("🔧 Delivery Exception Handler initialized with SCM Exception Handler v3");
        System.out.println("   Logging Enabled: " + loggingEnabled);
        System.out.println("   SCM Integration: " + scmIntegrationEnabled);
    }
}
