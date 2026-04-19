// integration/DatabaseOrderAdapter.java
package integration;

import integration.IDatabaseGateway.OrderData;

import java.util.Optional;

/**
 * Adapter that bridges the delivery subsystem's {@link OrderFulfillmentService}
 * interface to the database module's order data via {@link IDatabaseGateway}.
 *
 * <p>This replaces {@link MockOrderService} for production use. Instead of
 * blindly returning {@code true}, it queries the database to verify that
 * an order exists and its payment is confirmed.</p>
 *
 * <p><b>Design Pattern — Adapter (Structural):</b> Translates between the
 * delivery subsystem's API ({@code isOrderValid(int)}, {@code isPaymentDone(int)})
 * and the database module's API ({@code getOrder(String)} returning
 * {@code OrderData} with {@code paymentStatus}).</p>
 */
public class DatabaseOrderAdapter implements OrderFulfillmentService {

    private final IDatabaseGateway dbGateway;

    public DatabaseOrderAdapter(IDatabaseGateway dbGateway) {
        this.dbGateway = dbGateway;
        System.out.println("[DatabaseOrderAdapter] Connected to database gateway.");
    }

    /**
     * Checks whether the given order exists in the database.
     *
     * @param orderId the order ID to validate
     * @return {@code true} if the order is found in the database
     */
    @Override
    public boolean isOrderValid(int orderId) {
        String orderIdStr = String.valueOf(orderId);
        Optional<OrderData> order = dbGateway.getOrder(orderIdStr);

        if (order.isPresent()) {
            System.out.println("[DatabaseOrderAdapter] ✅ Order " + orderId
                    + " found in database (status=" + order.get().getOrderStatus() + ")");
        } else {
            System.out.println("[DatabaseOrderAdapter] ⚠ Order " + orderId
                    + " NOT found in database.");
        }

        return order.isPresent();
    }

    /**
     * Checks whether payment has been confirmed for the given order.
     *
     * @param orderId the order ID to check payment for
     * @return {@code true} if the order's payment status is "PAID"
     */
    @Override
    public boolean isPaymentDone(int orderId) {
        String orderIdStr = String.valueOf(orderId);
        Optional<OrderData> order = dbGateway.getOrder(orderIdStr);

        boolean paid = order
                .map(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .orElse(false);

        if (paid) {
            System.out.println("[DatabaseOrderAdapter] ✅ Order " + orderId
                    + " payment confirmed (PAID).");
        } else {
            String reason = order.map(o -> "status=" + o.getPaymentStatus())
                    .orElse("order not found");
            System.out.println("[DatabaseOrderAdapter] ⚠ Order " + orderId
                    + " payment NOT confirmed (" + reason + ").");
        }

        return paid;
    }
}
