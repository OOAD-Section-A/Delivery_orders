// integration/MockOrderService.java
package integration;

public class MockOrderService implements OrderFulfillmentService {

    public boolean isOrderValid(int orderId) {
        return orderId > 0;
    }

    public boolean isPaymentDone(int orderId) {
        return true; // assume paid
    }
}