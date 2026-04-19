// integration/StubDatabaseGateway.java
package integration;

import integration.IDatabaseGateway.DeliveryData;
import integration.IDatabaseGateway.OrderData;
import integration.IDatabaseGateway.TrackingEventData;

import java.math.BigDecimal;
import java.util.*;

/**
 * In-memory stub that simulates the SCM Database Module for standalone testing.
 *
 * <p>Pre-loaded with sample orders so the delivery subsystem can run
 * without MySQL or the database JAR on the classpath.</p>
 *
 * <p><b>Design Pattern — Stub / Test Double.</b></p>
 */
public class StubDatabaseGateway implements IDatabaseGateway {

    private final Map<String, DeliveryData> deliveries = new LinkedHashMap<>();
    private final Map<String, OrderData> orders = new LinkedHashMap<>();
    private final List<TrackingEventData> trackingEvents = new ArrayList<>();

    public StubDatabaseGateway() {
        // Sample orders matching what Main.java uses
        orders.put("101", new OrderData("101", "5001", "CONFIRMED", "PAID",
                BigDecimal.valueOf(1250.00)));
        orders.put("102", new OrderData("102", "5002", "CONFIRMED", "PAID",
                BigDecimal.valueOf(800.00)));
        orders.put("103", new OrderData("103", "5003", "CONFIRMED", "PENDING",
                BigDecimal.valueOf(450.00)));
        // Order 999 intentionally absent — used to test "not found" scenario

        System.out.println("[StubDatabaseGateway] Initialized with "
                + orders.size() + " sample orders.");
    }

    // -----------------------------------------------------------------
    // Delivery (Shipment) operations
    // -----------------------------------------------------------------

    @Override
    public void saveDelivery(DeliveryData delivery) {
        deliveries.put(delivery.getDeliveryId(), delivery);
        System.out.println("[StubDatabaseGateway] 💾 Saved delivery: " + delivery);
    }

    @Override
    public void updateDelivery(DeliveryData delivery) {
        deliveries.put(delivery.getDeliveryId(), delivery);
        System.out.println("[StubDatabaseGateway] 💾 Updated delivery: " + delivery);
    }

    @Override
    public Optional<DeliveryData> getDelivery(String deliveryId) {
        return Optional.ofNullable(deliveries.get(deliveryId));
    }

    @Override
    public List<DeliveryData> listDeliveries() {
        return Collections.unmodifiableList(new ArrayList<>(deliveries.values()));
    }

    // -----------------------------------------------------------------
    // Order operations
    // -----------------------------------------------------------------

    @Override
    public Optional<OrderData> getOrder(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    // -----------------------------------------------------------------
    // Tracking event operations
    // -----------------------------------------------------------------

    @Override
    public void logTrackingEvent(TrackingEventData event) {
        trackingEvents.add(event);
        System.out.println("[StubDatabaseGateway] 📍 Tracking event logged: " + event);
    }

    /** Returns all logged tracking events (for verification). */
    public List<TrackingEventData> getTrackingEvents() {
        return Collections.unmodifiableList(trackingEvents);
    }
}
