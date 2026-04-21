// integration/MockWarehouseService.java
package integration;

public class MockWarehouseService implements WarehouseService {

    public boolean isPacked(int orderId) {
        return true; // assume packed
    }
}