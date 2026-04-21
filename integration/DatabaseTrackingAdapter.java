// integration/DatabaseTrackingAdapter.java
package integration;

import integration.IDatabaseGateway.TrackingEventData;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adapter that bridges the delivery subsystem's {@link TrackingService}
 * interface to the database module via {@link IDatabaseGateway}.
 *
 * <p>This replaces {@link MockTrackingService} for production use. Instead
 * of just printing to console, it persists tracking events to the database
 * and updates the delivery record's status.</p>
 *
 * <p><b>Design Pattern — Adapter (Structural):</b> Translates between the
 * delivery subsystem's API ({@code updateStatus(int, String)}) and the
 * database module's API ({@code logTrackingEvent(...)} and
 * {@code updateDelivery(...)}).</p>
 */
public class DatabaseTrackingAdapter implements TrackingService {

    private final IDatabaseGateway dbGateway;

    public DatabaseTrackingAdapter(IDatabaseGateway dbGateway) {
        this.dbGateway = dbGateway;
        System.out.println("[DatabaseTrackingAdapter] Connected to database gateway.");
    }

    /**
     * Logs a tracking event and updates the delivery status in the database.
     *
     * @param deliveryId the delivery to update
     * @param status     the new status (e.g. "DISPATCHED", "DELIVERED")
     */
    @Override
    public void updateStatus(int deliveryId, String status) {
        String deliveryIdStr = String.valueOf(deliveryId);

        // 1. Log the tracking event
        TrackingEventData event = new TrackingEventData(
                UUID.randomUUID().toString(),
                deliveryIdStr,
                status,
                LocalDateTime.now()
        );
        dbGateway.logTrackingEvent(event);

        // 2. Update the delivery record's status
        dbGateway.getDelivery(deliveryIdStr).ifPresent(delivery -> {
            delivery.setStatus(status);
            dbGateway.updateDelivery(delivery);
        });

        System.out.println("[DatabaseTrackingAdapter] 📍 Delivery " + deliveryId
                + " → " + status + " (event logged + record updated)");
    }
}
