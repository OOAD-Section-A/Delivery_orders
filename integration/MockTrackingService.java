// integration/MockTrackingService.java
package integration;

public class MockTrackingService implements TrackingService {

    public void updateStatus(int deliveryId, String status) {
        System.out.println("Tracking अपडेट: Delivery " + deliveryId + " → " + status);
    }
}