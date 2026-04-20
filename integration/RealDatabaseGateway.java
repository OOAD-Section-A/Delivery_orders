// integration/RealDatabaseGateway.java
package integration;

import com.jackfruit.scm.database.facade.SupplyChainDatabaseFacade;
import com.jackfruit.scm.database.model.Order;
import com.jackfruit.scm.database.model.Shipment;
import com.jackfruit.scm.database.model.DeliveryMonitoringModels.DeliveryTrackingEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Real implementation of {@link IDatabaseGateway} that delegates to the
 * SCM Database Module (Team Jackfruit) via {@link SupplyChainDatabaseFacade}.
 *
 * <p>This class bridges our lightweight DTOs with the database module's
 * heavy-duty models. It requires the database JAR on the classpath.</p>
 */
public class RealDatabaseGateway implements IDatabaseGateway {

    private final SupplyChainDatabaseFacade facade;

    public RealDatabaseGateway(SupplyChainDatabaseFacade facade) {
        this.facade = facade;
        System.out.println("[RealDatabaseGateway] Connected to live SupplyChainDatabaseFacade.");
    }

    // -----------------------------------------------------------------
    // Delivery (Shipment) operations
    // -----------------------------------------------------------------

    @Override
    public void saveDelivery(DeliveryData data) {
        Shipment shipment = mapToShipment(data);
        facade.deliveryOrders().createDeliveryOrder(shipment);
        System.out.println("[RealDatabaseGateway] 🏦 Persisted new shipment " + data.getDeliveryId());
    }

    @Override
    public void updateDelivery(DeliveryData data) {
        Shipment shipment = mapToShipment(data);
        facade.deliveryOrders().updateDeliveryOrder(shipment);
        System.out.println("[RealDatabaseGateway] 🏦 Updated shipment " + data.getDeliveryId());
    }

    @Override
    public Optional<DeliveryData> getDelivery(String deliveryId) {
        return facade.deliveryOrders().getDeliveryOrder(deliveryId)
                .map(this::mapToDeliveryData);
    }

    @Override
    public List<DeliveryData> listDeliveries() {
        return facade.deliveryOrders().listDeliveryOrders().stream()
                .map(this::mapToDeliveryData)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------
    // Order operations
    // -----------------------------------------------------------------

    @Override
    public Optional<OrderData> getOrder(String orderId) {
        return facade.orders().getOrder(orderId)
                .map(this::mapToOrderData);
    }

    // -----------------------------------------------------------------
    // Tracking event operations
    // -----------------------------------------------------------------

    @Override
    public void logTrackingEvent(TrackingEventData data) {
        DeliveryTrackingEvent event = new DeliveryTrackingEvent(
                data.getEventId(),
                data.getDeliveryId(),
                "AGENT_SYSTEM", // riderId
                "VEHICLE-001",  // vehicleId
                data.getStage(),
                "0,0",          // gpsCoordinates
                data.getTimestamp(),
                "Status update: " + data.getStage(),
                false           // requiresRerouting
        );
        facade.deliveryMonitoring().createTrackingEvent(event);
        System.out.println("[RealDatabaseGateway] 🏦 Logged tracking event " + data.getEventId());
    }

    // -----------------------------------------------------------------
    // Mapping Helpers
    // -----------------------------------------------------------------

    private Shipment mapToShipment(DeliveryData data) {
        Shipment s = new Shipment();
        s.setDeliveryId(data.getDeliveryId());
        s.setOrderId(data.getOrderId());
        s.setCustomerId(data.getCustomerId());
        s.setDeliveryAddress(data.getAddress());
        s.setDeliveryStatus(data.getStatus());
        s.setAssignedAgent(data.getAssignedAgent());
        s.setDeliveryType(data.getDeliveryType());
        s.setDeliveryCost(data.getDeliveryCost());
        s.setWarehouseId(data.getWarehouseId());
        s.setCreatedAt(data.getCreatedAt());
        s.setUpdatedAt(data.getUpdatedAt());
        return s;
    }

    private DeliveryData mapToDeliveryData(Shipment s) {
        DeliveryData d = new DeliveryData();
        d.setDeliveryId(s.getDeliveryId());
        d.setOrderId(s.getOrderId());
        d.setCustomerId(s.getCustomerId());
        d.setAddress(s.getDeliveryAddress());
        d.setStatus(s.getDeliveryStatus());
        d.setAssignedAgent(s.getAssignedAgent());
        d.setDeliveryType(s.getDeliveryType());
        d.setDeliveryCost(s.getDeliveryCost());
        d.setWarehouseId(s.getWarehouseId());
        d.setCreatedAt(s.getCreatedAt());
        d.setUpdatedAt(s.getUpdatedAt());
        return d;
    }

    private OrderData mapToOrderData(Order o) {
        return new OrderData(
                o.getOrderId(),
                o.getCustomerId(),
                o.getOrderStatus(),
                o.getPaymentStatus(),
                o.getTotalAmount()
        );
    }
}
