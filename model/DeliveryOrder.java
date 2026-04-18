// model/DeliveryOrder.java
package model;

public class DeliveryOrder {
    public int deliveryId;
    public int orderId;
    public int customerId;
    public String address;
    public String status;
    public int assignedAgentId;

    public DeliveryOrder(int deliveryId, int orderId, int customerId, String address) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.address = address;
        this.status = "CREATED";
        this.assignedAgentId = -1; 
    }
}