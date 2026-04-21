// integration/CommissionWebhookService.java
package integration;

import model.DeliveryOrder;
import integration.IDatabaseGateway.OrderData;
import integration.IDatabaseGateway.DeliveryData;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Fires an HTTP POST webhook to the Commission subsystem whenever
 * a delivery is successfully completed (status → DELIVERED).
 *
 * <p><b>Integration Contract:</b> The Commission team provides a webhook
 * URL. When a delivery reaches DELIVERED status, this service sends a
 * JSON payload containing all data needed for commission calculation.</p>
 *
 * <p><b>Design Pattern — Observer (Behavioral):</b> This service acts as
 * an event listener on the delivery lifecycle. It is triggered by
 * {@code DeliveryOrderService.updateStatus()} when the new status is
 * DELIVERED.</p>
 *
 * <p><b>Payload Format:</b></p>
 * <pre>{@code
 * {
 *   "delivery_id": "1",
 *   "order_id": "101",
 *   "customer_id": "5001",
 *   "agent_id": "1",
 *   "agent_name": "Ravi",
 *   "delivery_address": "Bangalore",
 *   "order_total_amount": 1250.00,
 *   "delivery_cost": 50.00,
 *   "delivery_type": "STANDARD",
 *   "delivered_at": "2026-04-19T20:15:00"
 * }
 * }</pre>
 */
public class CommissionWebhookService {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String webhookUrl;
    private final IDatabaseGateway dbGateway;

    /**
     * @param webhookUrl the Commission team's webhook endpoint URL
     * @param dbGateway  database gateway for order amount lookup
     */
    public CommissionWebhookService(String webhookUrl, IDatabaseGateway dbGateway) {
        this.webhookUrl = webhookUrl;
        this.dbGateway = dbGateway;
        System.out.println("[CommissionWebhook] Registered webhook: " + webhookUrl);
    }

    /**
     * Fires the commission webhook for a completed delivery.
     * Called by {@code DeliveryOrderService} when status transitions to DELIVERED.
     *
     * @param order     the completed delivery order
     * @param agentName the name of the assigned delivery agent
     */
    public void notifyDeliveryCompleted(DeliveryOrder order, String agentName) {
        String json = buildPayload(order, agentName);

        System.out.println("[CommissionWebhook] 📤 Sending commission payload:");
        System.out.println(json);

        boolean sent = sendPost(json);

        if (sent) {
            System.out.println("[CommissionWebhook] ✅ Commission webhook delivered successfully.");
        } else {
            System.out.println("[CommissionWebhook] ⚠ Webhook POST failed (Commission team will "
                    + "not receive this event). Payload logged above for manual recovery.");
        }
    }

    /**
     * Builds the JSON payload for the commission webhook.
     * This is the exact structure the commission team should expect.
     */
    public String buildPayload(DeliveryOrder order, String agentName) {
        // Look up order total from DB
        double orderTotal = dbGateway.getOrder(String.valueOf(order.orderId))
                .map(o -> o.getTotalAmount().doubleValue())
                .orElse(0.0);

        // Look up delivery cost from DB
        double deliveryCost = dbGateway.getDelivery(String.valueOf(order.deliveryId))
                .map(d -> d.getDeliveryCost().doubleValue())
                .orElse(50.0); // default

        String deliveryType = dbGateway.getDelivery(String.valueOf(order.deliveryId))
                .map(DeliveryData::getDeliveryType)
                .orElse("STANDARD");

        String now = LocalDateTime.now().format(ISO_FMT);

        // Build JSON manually (no external lib dependency)
        return "{\n"
                + "  \"delivery_id\": \"" + order.deliveryId + "\",\n"
                + "  \"order_id\": \"" + order.orderId + "\",\n"
                + "  \"customer_id\": \"" + order.customerId + "\",\n"
                + "  \"agent_id\": \"" + order.assignedAgentId + "\",\n"
                + "  \"agent_name\": \"" + escape(agentName) + "\",\n"
                + "  \"delivery_address\": \"" + escape(order.address) + "\",\n"
                + "  \"order_total_amount\": " + orderTotal + ",\n"
                + "  \"delivery_cost\": " + deliveryCost + ",\n"
                + "  \"delivery_type\": \"" + deliveryType + "\",\n"
                + "  \"delivered_at\": \"" + now + "\"\n"
                + "}";
    }

    /**
     * Sends an HTTP POST to the webhook URL with the given JSON body.
     * Returns {@code true} if the server responded with 2xx.
     */
    private boolean sendPost(String jsonBody) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            // Expected to fail in standalone/stub mode (no live webhook server)
            System.out.println("[CommissionWebhook] POST failed: " + e.getMessage());
            return false;
        }
    }

    /** Simple JSON string escape. */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
