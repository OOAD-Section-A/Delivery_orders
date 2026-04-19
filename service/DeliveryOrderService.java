// service/DeliveryOrderService.java
package service;

import model.DeliveryOrder;
import model.DeliveryAgent;
import repository.DeliveryRepository;
import repository.AgentRepository;
import validator.DeliveryValidator;
import integration.*;
import exception.*;

public class DeliveryOrderService {

    private DeliveryRepository repo;
    private DeliveryValidator validator;
    private OrderFulfillmentService orderService;
    private WarehouseService warehouseService;
    private TrackingService trackingService;
    private AgentRepository agentRepo;
    private CommissionWebhookService commissionWebhook;
    private DeliveryStatusManager statusManager = new DeliveryStatusManager();

    public DeliveryOrderService(
        DeliveryRepository repo,
        DeliveryValidator validator,
        OrderFulfillmentService orderService,
        WarehouseService warehouseService,
        TrackingService trackingService,
        AgentRepository agentRepo,
        CommissionWebhookService commissionWebhook
    ) {
        this.repo = repo;
        this.validator = validator;
        this.orderService = orderService;
        this.warehouseService = warehouseService;
        this.trackingService = trackingService;
        this.agentRepo = agentRepo;
        this.commissionWebhook = commissionWebhook;
    }

    public DeliveryOrder createDelivery(int deliveryId, int orderId, int customerId, String address)
            throws InvalidOrderException, PackingNotConfirmedException {

        if (!orderService.isOrderValid(orderId))
            throw new InvalidOrderException("Order not found");

        if (!orderService.isPaymentDone(orderId))
            throw new InvalidOrderException("Payment not confirmed");

        if (!validator.validateAddress(address))
            throw new InvalidOrderException("Invalid address");

        if (!warehouseService.isPacked(orderId))
            throw new PackingNotConfirmedException("Order not packed");

        DeliveryOrder order = new DeliveryOrder(deliveryId, orderId, customerId, address);
        repo.save(order);

        System.out.println("✅ Delivery Created: " + deliveryId);

        return order;
    }

public void updateStatus(int deliveryId, String newStatus) {

    DeliveryOrder order = repo.find(deliveryId);

    if (order == null) {
        System.out.println("❌ Delivery not found");
        return;
    }

    String currentStatus = order.status;

    if (!statusManager.isValidTransition(currentStatus, newStatus)) {
        System.out.println("❌ Invalid status transition: " 
            + currentStatus + " → " + newStatus);
        return;
    }

    order.status = newStatus;

    System.out.println("✅ Status updated: " + newStatus);

    trackingService.updateStatus(deliveryId, newStatus);

    // 🔔 Fire commission webhook when delivery is completed
    if ("DELIVERED".equals(newStatus) && commissionWebhook != null) {
        DeliveryAgent agent = agentRepo.findAgentById(order.assignedAgentId);
        String agentName = (agent != null) ? agent.name : "Unknown";
        commissionWebhook.notifyDeliveryCompleted(order, agentName);
    }
}

public void assignAgent(int deliveryId) throws AgentAssignmentFailedException {

    DeliveryOrder order = repo.find(deliveryId);

    if (order == null) {
        System.out.println("❌ Delivery not found");
        return;
    }

    DeliveryAgent agent = agentRepo.findAvailableAgent();

    if (agent == null) {
        throw new AgentAssignmentFailedException("No agents available");
    }

    order.assignedAgentId = agent.agentId;
    agentRepo.markUnavailable(agent.agentId);

    System.out.println("🚚 Agent assigned: " + agent.name);
}
}