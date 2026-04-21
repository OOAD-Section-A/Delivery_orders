// integration/IDatabaseGateway.java
package integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the SCM Database Module (Team Jackfruit).
 *
 * <p>This interface mirrors the subset of {@code SupplyChainDatabaseFacade}
 * operations relevant to the delivery subsystem. Our adapters depend on
 * this interface — not on the concrete JAR classes.</p>
 *
 * <p><b>Design Pattern — Adapter (Structural):</b> When the database JAR is
 * on the classpath, a thin wrapper maps these methods to the real facade.
 * Without the JAR, {@link StubDatabaseGateway} provides in-memory data.</p>
 *
 * <p><b>Maps to:</b></p>
 * <ul>
 *   <li>{@code facade.deliveryOrders()} → Shipment CRUD</li>
 *   <li>{@code facade.orders()} → Order/payment validation</li>
 *   <li>{@code facade.deliveryMonitoring()} → Tracking events</li>
 * </ul>
 */
public interface IDatabaseGateway {

    // -----------------------------------------------------------------
    // Delivery (Shipment) operations
    // -----------------------------------------------------------------

    void saveDelivery(DeliveryData delivery);

    void updateDelivery(DeliveryData delivery);

    Optional<DeliveryData> getDelivery(String deliveryId);

    List<DeliveryData> listDeliveries();

    // -----------------------------------------------------------------
    // Order operations
    // -----------------------------------------------------------------

    Optional<OrderData> getOrder(String orderId);

    // -----------------------------------------------------------------
    // Tracking event operations
    // -----------------------------------------------------------------

    void logTrackingEvent(TrackingEventData event);

    // =================================================================
    // DTO classes — lightweight mirrors of the DB module's model objects
    // =================================================================

    /**
     * Mirrors {@code com.jackfruit.scm.database.model.Shipment}.
     */
    class DeliveryData {
        private String deliveryId;
        private String orderId;
        private String customerId;
        private String address;
        private String status;
        private String assignedAgent;
        private String deliveryType;
        private BigDecimal deliveryCost;
        private String warehouseId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DeliveryData() {}

        public DeliveryData(String deliveryId, String orderId, String customerId,
                            String address, String status) {
            this.deliveryId = deliveryId;
            this.orderId = orderId;
            this.customerId = customerId;
            this.address = address;
            this.status = status;
            this.deliveryType = "STANDARD";
            this.deliveryCost = BigDecimal.valueOf(50.00);
            this.warehouseId = "WH-DEFAULT";
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        // --- Getters ---
        public String getDeliveryId()    { return deliveryId; }
        public String getOrderId()       { return orderId; }
        public String getCustomerId()    { return customerId; }
        public String getAddress()       { return address; }
        public String getStatus()        { return status; }
        public String getAssignedAgent() { return assignedAgent; }
        public String getDeliveryType()  { return deliveryType; }
        public BigDecimal getDeliveryCost() { return deliveryCost; }
        public String getWarehouseId()   { return warehouseId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        // --- Setters ---
        public void setDeliveryId(String v)    { this.deliveryId = v; }
        public void setOrderId(String v)       { this.orderId = v; }
        public void setCustomerId(String v)    { this.customerId = v; }
        public void setAddress(String v)       { this.address = v; }
        public void setStatus(String v)        { this.status = v; this.updatedAt = LocalDateTime.now(); }
        public void setAssignedAgent(String v) { this.assignedAgent = v; }
        public void setDeliveryType(String v)  { this.deliveryType = v; }
        public void setDeliveryCost(BigDecimal v) { this.deliveryCost = v; }
        public void setWarehouseId(String v)   { this.warehouseId = v; }
        public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
        public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }

        @Override
        public String toString() {
            return String.format("Delivery[%s, order=%s, status=%s, agent=%s]",
                    deliveryId, orderId, status, assignedAgent);
        }
    }

    /**
     * Mirrors {@code com.jackfruit.scm.database.model.Order}.
     */
    class OrderData {
        private final String orderId;
        private final String customerId;
        private final String orderStatus;
        private final String paymentStatus;
        private final BigDecimal totalAmount;

        public OrderData(String orderId, String customerId, String orderStatus,
                         String paymentStatus, BigDecimal totalAmount) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.orderStatus = orderStatus;
            this.paymentStatus = paymentStatus;
            this.totalAmount = totalAmount;
        }

        public String getOrderId()       { return orderId; }
        public String getCustomerId()    { return customerId; }
        public String getOrderStatus()   { return orderStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public BigDecimal getTotalAmount() { return totalAmount; }

        @Override
        public String toString() {
            return String.format("Order[%s, status=%s, payment=%s, amount=%s]",
                    orderId, orderStatus, paymentStatus, totalAmount);
        }
    }

    /**
     * Mirrors {@code DeliveryMonitoringModels.DeliveryTrackingEvent}.
     */
    class TrackingEventData {
        private final String eventId;
        private final String deliveryId;
        private final String stage;
        private final LocalDateTime timestamp;

        public TrackingEventData(String eventId, String deliveryId,
                                 String stage, LocalDateTime timestamp) {
            this.eventId = eventId;
            this.deliveryId = deliveryId;
            this.stage = stage;
            this.timestamp = timestamp;
        }

        public String getEventId()      { return eventId; }
        public String getDeliveryId()   { return deliveryId; }
        public String getStage()        { return stage; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("TrackingEvent[%s, delivery=%s, stage=%s]",
                    eventId, deliveryId, stage);
        }
    }
}
