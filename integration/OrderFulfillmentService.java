// integration/OrderFulfillmentService.java
package integration;

public interface OrderFulfillmentService {
    boolean isOrderValid(int orderId);
    boolean isPaymentDone(int orderId);
}