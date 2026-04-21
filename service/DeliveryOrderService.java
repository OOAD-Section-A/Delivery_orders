// service/DeliveryOrderService.java
package service;

import model.DeliveryOrder;
import model.DeliveryAgent;
import repository.DeliveryRepository;
import repository.AgentRepository;
import validator.DeliveryValidator;
import integration.*;
import exception.*;
import exception.DeliveryExceptionHandler;

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
            CommissionWebhookService commissionWebhook) {
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

        try {
            if (!orderService.isOrderValid(orderId)) {
                InvalidOrderException e = new InvalidOrderException("Order not found", "ORDER_NOT_FOUND");
                DeliveryExceptionHandler.handleInvalidOrderException(e);
                throw e;
            }

            if (!orderService.isPaymentDone(orderId)) {
                InvalidOrderException e = new InvalidOrderException("Payment not confirmed", "PAYMENT_PENDING");
                DeliveryExceptionHandler.handleInvalidOrderException(e);
                throw e;
            }

            if (!validator.validateAddress(address)) {
                InvalidOrderException e = new InvalidOrderException("Invalid address", "INVALID_ADDRESS");
                DeliveryExceptionHandler.handleInvalidOrderException(e);
                throw e;
            }

            if (!warehouseService.isPacked(orderId)) {
                PackingNotConfirmedException e = new PackingNotConfirmedException("Order not packed",
                        "ORDER_NOT_PACKED");
                DeliveryExceptionHandler.handlePackingException(e);
                throw e;
            }

            DeliveryOrder order = new DeliveryOrder(deliveryId, orderId, customerId, address);
            repo.save(order);

            System.out.println("✅ Delivery Created: " + deliveryId);

            return order;
        } catch (InvalidOrderException | PackingNotConfirmedException e) {
            throw e;
        } catch (Exception e) {
            InvalidOrderException deliveryEx = new InvalidOrderException("Unexpected error during delivery creation",
                    "DELIVERY_CREATE_ERROR");
            DeliveryExceptionHandler.handleDeliveryException(deliveryEx, "CREATE_DELIVERY");
            throw deliveryEx;
        }
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
            AgentAssignmentFailedException e = new AgentAssignmentFailedException("No agents available",
                    "NO_AGENTS_AVAILABLE");
            DeliveryExceptionHandler.handleAgentAssignmentException(e);
            throw e;
        }

        order.assignedAgentId = agent.agentId;
        agentRepo.markUnavailable(agent.agentId);

        System.out.println("🚚 Agent assigned: " + agent.name);
    }
}